/**
 *    Copyright 2013 Jadaptive Limited
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.jadaptive.onedrive.niofs.api;

import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OneDriveRemoteAPICaller {

    private final GraphServiceClient graphServiceClient;
    private final Drive drive;

    public OneDriveRemoteAPICaller(GraphServiceClient graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
        this.drive = getDrive().orElseThrow(() -> new IllegalStateException("No drive found."));
    }

    public OneDriveRemoteAPICaller(GraphServiceClient graphServiceClient, String id) {
        this.graphServiceClient = graphServiceClient;
        this.drive = getDriveById(id).orElseThrow(() -> new IllegalStateException("No drive found."));
    }

    public User getUser() {
        return graphServiceClient.me().get();
    }

    public Optional<Drive> getDrive() {
        var drives = graphServiceClient.me().drives().get().getValue();

        if (drives != null) {

            if (drives.size() != 1) {
                throw new IllegalStateException("No drive or more than one drive.");
            }

            return Optional.ofNullable(drives.get(0));
        }

        return Optional.empty();
    }

    public Optional<Drive> getDriveById(String id) {
        return getDrive(d -> Objects.equals(d.getName(), id));
    }

    public Optional<Drive> getDriveByName(String name) {
        return getDrive(d -> Objects.equals(d.getName(), name));
    }

    public Optional<DriveItem> getDriveRootItem() {

        var driveId = this.drive.getId();

        Objects.requireNonNull(driveId, "Drive id cannot be null.");

        return Optional.ofNullable(graphServiceClient.drives().byDriveId(driveId).root().get());

    }

    public List<DriveItem> getDriveItems(DriveItem parent) {

        var driveId = this.drive.getId();

        Objects.requireNonNull(driveId, "Drive id cannot be null.");

        var parentId = parent.getId();

        Objects.requireNonNull(driveId, "Parent drive item cannot be null.");

        var result = graphServiceClient.drives()
                .byDriveId(driveId).items()
                .byDriveItemId(parentId).children().get().getValue();

        return result == null ? Collections.emptyList() : result;
    }

    public DriveItem createFolder(String folderName, DriveItem parentFolder) {

        Objects.requireNonNull(folderName,"Folder name cannot be null.");

        Objects.requireNonNull(parentFolder,"Parent folder cannot be null");
        Objects.requireNonNull(parentFolder.getId(),"Parent folder Id cannot be null.");

        var driveId = this.drive.getId();

        var driveItem = new DriveItem();
        driveItem.setName(folderName);
        var folder = new Folder();
        driveItem.setFolder(folder);

        return graphServiceClient.drives()
                .byDriveId(driveId).items()
                .byDriveItemId(parentFolder.getId())
                .children()
                .post(driveItem);
    }

    public void deleteDriveItem(DriveItem driveItem) {

        Objects.requireNonNull(driveItem,"Drive item cannot be null");
        Objects.requireNonNull(driveItem.getId(),"Drive item Id cannot be null.");

        var driveId = this.drive.getId();

        graphServiceClient.drives()
                .byDriveId(driveId)
                .items()
                .byDriveItemId(driveItem.getId())
                .delete();
    }

    private Optional<Drive> getDrive(Predicate<? super Drive> filter) {
        var drives = graphServiceClient.me().drives().get().getValue();

        if (drives != null) {

            var foundDrives = drives.stream()
                    .filter(filter)
                    .collect(Collectors.toSet());

            if (foundDrives.size() != 1) {
                throw new IllegalStateException("No drive or more than one drive.");
            }

            return foundDrives.stream().findFirst();
        }

        return Optional.empty();
    }
}

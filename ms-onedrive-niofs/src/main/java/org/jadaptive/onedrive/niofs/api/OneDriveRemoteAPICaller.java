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

import com.microsoft.graph.drives.item.items.item.copy.CopyPostRequestBody;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.RequestAdapter;
import org.jadaptive.api.folder.JadFsResource;
import org.jadaptive.niofs.attr.JadNioFileAttributes;

import java.nio.file.LinkOption;
import java.nio.file.attribute.FileTime;
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
        this.drive = eagerLoadDriveInfo().orElseThrow(() -> new IllegalStateException("No drive found."));
    }

    public OneDriveRemoteAPICaller(GraphServiceClient graphServiceClient, String id) {
        this.graphServiceClient = graphServiceClient;
        this.drive = getDriveById(id).orElseThrow(() -> new IllegalStateException("No drive found."));
    }

    public User getUser() {
        return graphServiceClient.me().get();
    }

    public Drive getDrive() {
        return drive;
    }

    public Optional<Drive> getDriveById(String id) {
        return getDrive(d -> Objects.equals(d.getName(), id));
    }

    public Optional<Drive> getDriveByName(String name) {
        return getDrive(d -> Objects.equals(d.getName(), name));
    }

    public Optional<DriveItem> getDriveRootItem() {

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        return Optional.ofNullable(graphServiceClient.drives().byDriveId(driveId).root().get());

    }

    public List<DriveItem> getDriveItems(JadFsResource parentResource) {
        var parentDriveItem = new DriveItem();
        parentDriveItem.setId(parentResource.id);
        parentDriveItem.setName(parentResource.name);

        return getDriveItems(parentDriveItem);
    }

    public List<DriveItem> getDriveItems(DriveItem parent) {

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        var parentId = Objects.requireNonNull(parent.getId(), "Parent drive item id cannot be null.");

        var result = Objects.requireNonNull(graphServiceClient.drives()
                .byDriveId(driveId).items()
                .byDriveItemId(parentId).children().get()).getValue();

        return result == null ? Collections.emptyList() : result;
    }

    public Optional<DriveItem> getDriveItem(JadFsResource resource) {

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        var resourceId = Objects.requireNonNull(resource.id, "Resource item id cannot be null.");

        return Optional.ofNullable(graphServiceClient.drives()
                .byDriveId(driveId)
                .items()
                .byDriveItemId(resourceId)
                .get());
    }

    public DriveItem createFolder(String folderName, JadFsResource parentFolder) {

        Objects.requireNonNull(folderName,"Folder name cannot be null.");

        Objects.requireNonNull(parentFolder,"Parent folder cannot be null");
        Objects.requireNonNull(parentFolder.id,"Parent folder Id cannot be null.");

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        var driveItem = new DriveItem();
        driveItem.setName(folderName);
        var folder = new Folder();
        driveItem.setFolder(folder);

        return graphServiceClient.drives()
                .byDriveId(driveId).items()
                .byDriveItemId(parentFolder.id)
                .children()
                .post(driveItem);
    }

    public void delete(JadFsResource resource) {

        Objects.requireNonNull(resource,"Resource item cannot be null");
        Objects.requireNonNull(resource.id,"Resource item Id cannot be null.");

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        graphServiceClient.drives()
                .byDriveId(driveId)
                .items()
                .byDriveItemId(resource.id)
                .delete();
    }

    public DriveItem copy(JadFsResource source, JadFsResource parent, String targetName) {

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        var parentReference = new ItemReference();
        parentReference.setDriveId(driveId);
        parentReference.setId(parent.id);

        var copyPostRequestBody = new CopyPostRequestBody();
        copyPostRequestBody.setParentReference(parentReference);
        copyPostRequestBody.setName(targetName);

        return graphServiceClient.drives()
                .byDriveId(driveId)
                .items()
                .byDriveItemId(source.id)
                .copy()
                .post(copyPostRequestBody);

    }

    public DriveItem move(JadFsResource source, JadFsResource parent, String targetName) {

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        ItemReference parentReference = new ItemReference();
        parentReference.setId(parent.id);

        DriveItem driveItem = new DriveItem();
        driveItem.setParentReference(parentReference);
        driveItem.setName(targetName);

        return graphServiceClient.drives()
                .byDriveId(driveId)
                .items()
                .byDriveItemId(source.id)
                .patch(driveItem);

    }

    public JadNioFileAttributes getJadAttributesForPath(JadFsResource resource, LinkOption... options) {

        var item = getDriveItem(resource)
                .orElseThrow(() -> new IllegalArgumentException("No remote resource found."));

        var regularFile = isRegularFile(item);

        var size = item.getSize();
        var fileKey = item.getId();


        var creationTime = item.getCreatedDateTime() == null
                ? FileTime.fromMillis(0)
                : FileTime.from(item.getCreatedDateTime().toInstant());

        var fileAttributes = new JadNioFileAttributes(creationTime, regularFile, size, fileKey);

        if (item.getLastModifiedDateTime() != null) {
            var lastModifiedTime = FileTime.from(item.getLastModifiedDateTime().toInstant());
            fileAttributes.setLastModifiedTime(lastModifiedTime);
        }

        return fileAttributes;
    }

    public String getDriveItemDownloadUrl(JadFsResource resource) {
        var item = getDriveItem(resource).orElseThrow(() -> new IllegalArgumentException("No remote resource found."));
        var additionalData = Objects.requireNonNull(item.getAdditionalData(), "No additional data found, is null");
        return (String) Objects.requireNonNull(additionalData.get("@microsoft.graph.downloadUrl"), "Download URL is null.");
    }

    public String getUploadUrl(String itemPath) {
        var uploadSession = createUploadSession(Objects.requireNonNull(itemPath,"Item path cannot be null."));
        return uploadSession.getUploadUrl();
    }

    public RequestAdapter getRequestAdapter() {
        return this.graphServiceClient.getRequestAdapter();
    }

    public UploadSession createUploadSession(String itemPath) {

        Objects.requireNonNull(itemPath,"Item path cannot be null.");

        var driveId = Objects.requireNonNull(this.drive.getId(), "Drive id cannot be null.");

        // Set body of the upload session request
        var uploadSessionRequest = new CreateUploadSessionPostRequestBody();
        var properties = new DriveItemUploadableProperties();
        properties.getAdditionalData().put("@microsoft.graph.conflictBehavior", "replace");
        uploadSessionRequest.setItem(properties);

        var relativeToRootItemPath = String.format("root:%s:", itemPath);

        System.out.println("Uploading to relative path at " + relativeToRootItemPath);

        // Create an upload session
        // ItemPath does not need to be a path to an existing item
        return graphServiceClient.drives()
                .byDriveId(driveId)
                .items()
                .byDriveItemId(relativeToRootItemPath)
                .createUploadSession()
                .post(uploadSessionRequest);
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

    private static boolean isRegularFile(DriveItem item) {

        Objects.requireNonNull(item, "Drive item cannot be null");

        return item.getFile() != null;
    }

    private Optional<Drive> eagerLoadDriveInfo() {
        var drives = Objects.requireNonNull(graphServiceClient.me().drives().get()).getValue();

        if (drives != null) {

            if (drives.size() != 1) {
                throw new IllegalStateException("No drive or more than one drive.");
            }

            return Optional.ofNullable(drives.get(0));
        }

        return Optional.empty();
    }
}

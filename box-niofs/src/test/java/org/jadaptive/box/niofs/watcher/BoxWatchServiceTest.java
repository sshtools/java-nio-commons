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
package org.jadaptive.box.niofs.watcher;

import org.jadaptive.box.niofs.filesysprovider.BoxFileSystemProvider;
import org.jadaptive.box.niofs.setup.AbstractRemoteSetup;
import org.jadaptive.niofs.exception.JadNioFsFileNotFoundException;
import org.jadaptive.niofs.path.BasePath;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.concurrent.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BoxWatchServiceTest extends AbstractRemoteSetup {

    private static final String BASE_DIRECTORY = "box:///test_box_watch_service";
    private static final String DIRECTORY_CREATE = "create_test";
    private static final String DIRECTORY_MODIFY = "modify_test";
    private static final String DIRECTORY_DELETE = "delete_test";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    @BeforeAll
    void setup() throws Exception {
        super.init();
    }

    @Test
    @DisplayName("It should catch the event fired by watcher service when a file is created in a watched directory")
    void testShouldCatchEventWhenNewFileIsCreatedInWatchDirectory() throws InterruptedException, IOException {
        var fileNameToTest = "file_to_watch_modify.txt";

        var countDownLatch = new CountDownLatch(2);

        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = (BasePath) provider.getPath(toURI(BASE_DIRECTORY)).resolve(DIRECTORY_CREATE);

        provider.createDirectory(sourceDirectory);

        var activityWatchService = new Activity<>(countDownLatch, () ->
                watchServiceAction(sourceDirectory,
                        kind(StandardWatchEventKinds.ENTRY_CREATE))
        );

        EXECUTOR_SERVICE.execute(() -> activityWatchService.run());

        // let the watch service get ready
        sleep(5);

        var activityAddFreshFile = new Activity<>(countDownLatch, () -> {
            writeFileInBox(provider, sourceDirectory, fileNameToTest);
            return null;
        });

        EXECUTOR_SERVICE.execute(() -> activityAddFreshFile.run());

        countDownLatch.await(1, TimeUnit.MINUTES);

        Assertions.assertEquals(fileNameToTest, activityWatchService.getResult());
    }

    @Test
    @DisplayName("It should catch the event fired by watcher service when a file is modified in a watched directory")
     void testShouldCatchEventWhenNewFileIsModifiedInWatchDirectory() throws InterruptedException, IOException {
        var fileNameToTest = "file_to_watch_modify.txt";

        var countDownLatch = new CountDownLatch(2);

        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = (BasePath) provider.getPath(toURI(BASE_DIRECTORY)).resolve(DIRECTORY_MODIFY);

        provider.createDirectory(sourceDirectory);

        writeFileInBox(provider, sourceDirectory, fileNameToTest);

        // modify depends on time stamp we need some time difference
        sleep(120); // 2 minutes should give good difference and remote api to settle

        var activityWatchService = new Activity<>(countDownLatch, () ->
                watchServiceAction(sourceDirectory,
                        kind(StandardWatchEventKinds.ENTRY_MODIFY))
        );

        EXECUTOR_SERVICE.execute(() -> activityWatchService.run());

        // let the watch service get ready
        sleep(5);

        var activityAddFreshFile = new Activity<>(countDownLatch, () -> {
            writeFileInBox(provider, sourceDirectory, fileNameToTest);
            return null;
        });

        EXECUTOR_SERVICE.execute(() -> activityAddFreshFile.run());

        countDownLatch.await(1, TimeUnit.MINUTES);

        Assertions.assertEquals(fileNameToTest, activityWatchService.getResult());
    }

    @Test
    @DisplayName("It should catch the event fired by watcher service when a file is deleted in a watched directory")
    void testShouldCatchEventWhenNewFileIsDeletedInWatchDirectory() throws InterruptedException, IOException {
        var fileNameToTest = "file_to_watch_delete.txt";

        var countDownLatch = new CountDownLatch(2);

        var provider = getNewBoxFileSystemProvider();

        var sourceDirectory = (BasePath) provider.getPath(toURI(BASE_DIRECTORY)).resolve(DIRECTORY_DELETE);

        provider.createDirectory(sourceDirectory);

        writeFileInBox(provider, sourceDirectory, fileNameToTest);

        sleep(30); // 30 seconds should give good difference and remote api to settle

        var activityWatchService = new Activity<>(countDownLatch, () ->
                watchServiceAction(sourceDirectory,
                        kind(StandardWatchEventKinds.ENTRY_DELETE))
        );

        EXECUTOR_SERVICE.execute(() -> activityWatchService.run());

        // let the watch service get ready
        sleep(5);

        var activityAddFreshFile = new Activity<>(countDownLatch, () -> {
            Files.delete(provider.getPath(sourceDirectory.resolve(fileNameToTest).toUri()));
            return null;
        });

        EXECUTOR_SERVICE.execute(() -> activityAddFreshFile.run());

        countDownLatch.await(1, TimeUnit.MINUTES);

        Assertions.assertEquals(fileNameToTest, activityWatchService.getResult());
    }

    private static class Activity<R> {

        private final CountDownLatch countDownLatch;
        private final Callable<R> callable;
        private R result;

        Activity(CountDownLatch countDownLatch, Callable<R> callable) {
            this.countDownLatch = countDownLatch;
            this.callable = callable;
        }

        void run() {
            try {
                this.result = this.callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                this.countDownLatch.countDown();
            }
        }

        public R getResult() {
            return result;
        }
    }

    private <T> WatchEvent.Kind<T>[] kind(WatchEvent.Kind<T> kind) {
        return new WatchEvent.Kind[] {kind};
    }
    private <T> String watchServiceAction(BasePath sourceDirectory, WatchEvent.Kind<T>[] kinds) {
        try {

            String fileName = "";
            try (var watchService = new BoxWatcherService(10)) {

                watchService.register(sourceDirectory, kinds);

                WatchKey key;
                out: while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        // React to new file.
                        fileName = event.context().toString();
                        break out;
                    }
                    // Do not reset which enqueues for event again, we have to catch it once in testing
                    //key.reset();
                }
            }

            return fileName;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFileInBox(BoxFileSystemProvider provider, BasePath sourceDirectory, String file) throws IOException {
        var fileToWrite = provider.getPath(sourceDirectory.resolve(file).toUri());
        var localFile = Path.of(String.format("src/test/resources/data/%s", file));
        writeFileInBox(provider, fileToWrite, localFile);
    }

    @Override
    protected void setUpTestData() throws Exception {
        var provider = getNewBoxFileSystemProvider();
        var path = provider.getPath(new URI(BASE_DIRECTORY));

        try {
            provider.delete(path);
        } catch (JadNioFsFileNotFoundException e) {}

        provider.createDirectory(path);
    }
}

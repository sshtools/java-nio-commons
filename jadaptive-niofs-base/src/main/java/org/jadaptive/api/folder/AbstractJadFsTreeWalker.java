package org.jadaptive.api.folder;

import java.util.Collection;

public abstract class AbstractJadFsTreeWalker implements JadFsResource.JadFsTreeWalker {

    protected abstract JadFsResource.JadFsResourceMapper getMapper();

    public JadFsResource walk(Collection<String> pathToCheck) {
        return JadFsResourceFolderTree.walk(pathToCheck, new JadFsResource.JadFsResourceChildrenFetcher() {
            @Override
            public Iterable<JadFsResource> children(JadFsResource jadFsResource) {
                return () -> getMapper().iterator(jadFsResource);
            }

            @Override
            public JadFsResource root() {
                return getMapper().root();
            }
        });
    }

}

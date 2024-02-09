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
package org.jadaptive.niofs.path;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BasePathService {
    protected FileSystem fileSystem;

    public BasePathService() {
    }

    public abstract FileSystem getFileSystem();
    public abstract void setFileSystem(FileSystem fileSystem);
    public abstract String getRootName();
    public abstract BasePath createRoot();
    public abstract BasePath createPathFromIndex(int beginIndex, int endIndex, List<String> names);
    public abstract BasePath createPath(String root, List<String> names);
    public BasePath createEmptyPath() {
        return createPath(null, List.of(""));
    }
    public abstract BasePath getPath(String[] paths);
    public abstract BasePath getPath(String first, String... more);
    public abstract BasePath getPath(URI uri);
    public abstract BasePath getWorkingDirectory();

    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        int colonIndex = syntaxAndPattern.indexOf(':');
        if ((colonIndex <= 0) || (colonIndex == syntaxAndPattern.length() - 1)) {
            throw new IllegalArgumentException(
                    "syntaxAndPattern must have form \"syntax:pattern\" but was \"" + syntaxAndPattern + "\"");
        }

        String syntax = syntaxAndPattern.substring(0, colonIndex);
        String pattern = syntaxAndPattern.substring(colonIndex + 1);
        String expr;
        switch (syntax) {
            case "glob":
                expr = globToRegex(pattern);
                break;
            case "regex":
                expr = pattern;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported path matcher syntax: \'" + syntax + "\'");
        }
        final Pattern regex = Pattern.compile(expr);
        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                String str = path.toString();
                Matcher m = regex.matcher(str);
                return m.matches();
            }
        };
    }

    protected String globToRegex(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        int inGroup = 0;
        int inClass = 0;
        boolean inQE = false;
        int firstIndexInClass = -1;
        char[] arr = pattern.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            switch (ch) {
                case '\\':
                    if (++i >= arr.length) {
                        sb.append('\\');
                    } else {
                        char next = arr[i];
                        switch (next) {
                            case ',':
                                // escape not needed
                                break;
                            case 'Q':
                                inQE = true;
                                sb.append("\\");
                                break;
                            case 'E':
                                // extra escape needed
                                inQE = false;
                                sb.append("\\");
                                break;
                            default:
                                sb.append('\\');
                                break;
                        }
                        sb.append(next);
                    }
                    break;
                default:
                    if (inQE)
                        sb.append(ch);
                    else {
                        switch (ch) {

                            case '*':
                                sb.append(inClass == 0 ? ".*" : "*");
                                break;
                            case '?':
                                sb.append(inClass == 0 ? '.' : '?');
                                break;
                            case '[':
                                inClass++;
                                firstIndexInClass = i + 1;
                                sb.append('[');
                                break;
                            case ']':
                                inClass--;
                                sb.append(']');
                                break;
                            case '.':
                            case '(':
                            case ')':
                            case '+':
                            case '|':
                            case '^':
                            case '$':
                            case '@':
                            case '%':
                                if (inClass == 0 || (firstIndexInClass == i && ch == '^')) {
                                    sb.append('\\');
                                }
                                sb.append(ch);
                                break;
                            case '!':
                                sb.append(firstIndexInClass == i ? '^' : '!');
                                break;
                            case '{':
                                inGroup++;
                                sb.append('(');
                                break;
                            case '}':
                                inGroup--;
                                sb.append(')');
                                break;
                            case ',':
                                sb.append(inGroup > 0 ? '|' : ',');
                                break;
                            default:
                                sb.append(ch);
                        }
                    }
                    break;
            }
        }
        return sb.toString();
    }

}

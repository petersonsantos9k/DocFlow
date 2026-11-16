package com.docflow.ingestion.source;

import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.model.RepositorySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class GitHubRepositoryUrlParser {

    private static final int MAX_URL_LENGTH = 200;

    private static final List<String> ACCEPTED_PREFIXES = List.of(
            "https://github.com/",
            "https://www.github.com/"
    );

    private static final String CANONICAL_PREFIX = "https://github.com/";

    private static final Pattern OWNER =
            Pattern.compile("[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}");
    private static final Pattern PATH_SEGMENT =
            Pattern.compile("[A-Za-z0-9._-]+");
    private static final Pattern TRAILING_SLASHES = Pattern.compile("/+$");

    private static final Set<String> UNSUPPORTED_GITHUB_PATHS =
            Set.of("tree", "blob", "commit");

    public RepositorySource validateAndExtract(String repositoryUrl) {

        String path = normalize(repositoryUrl);
        String[] parts = path.split("/", 3);

        if (parts.length < 2) {
            throw new SourceException(
                    "Repository URL must include both owner and repository name.");
        }

        String owner = parts[0];
        String name = stripGitSuffix(parts[1]);
        String subPath = parts.length > 2 ? parts[2] : null;
        String fullPath = subPath == null ? name : name + "/" + subPath;

        validateOwner(owner);
        validateName(name);
        validateRepositoryPath(fullPath);
        validateSubPath(subPath);

        return new RepositorySource(owner, name, subPath,  CANONICAL_PREFIX + owner + "/" + name, repositoryUrl);
    }

    private String normalize(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new SourceException("Repository URL must not be empty.");
        }

        String url = repositoryUrl.trim();
        String prefix = findAcceptedPrefix(url);

        if (url.length() > MAX_URL_LENGTH) {
            throw new SourceException(
                    "Repository URL exceeds the maximum length of "
                            + MAX_URL_LENGTH + " characters.");
        }

        String path = url.substring(prefix.length());
        path = TRAILING_SLASHES.matcher(path).replaceAll("");

        if(path.isEmpty()){
            throw new SourceException(
                    "Repository URL must include both owner and repository name.");
        }

        return path;
    }


    private String findAcceptedPrefix(String url){
        String prefix = ACCEPTED_PREFIXES.stream()
                .filter(p -> url.regionMatches(true, 0, p, 0, p.length()))
                .findFirst()
                .orElseThrow(() -> new SourceException(
                        "Unsupported repository URL. Only GitHub HTTPS URLs are supported."));

        if(url.length() == prefix.length()){
            throw new SourceException(
                    "Repository URL must include both owner and repository name.");
        }

        return prefix;
    }


    private String stripGitSuffix(String name) {
        int len = name.length();
        return len > 4 && name.regionMatches(true, len - 4, ".git", 0, 4)
                ? name.substring(0, len - 4)
                : name;
    }

    private void validateOwner(String owner) {
        if (!OWNER.matcher(owner).matches()) {
            throw new SourceException(
                    "Invalid GitHub owner in repository URL: '" + owner + "'.");
        }
    }

    private void validateName(String name){
        if(name.isEmpty()
                || name.equals(".")
                || name.equals("..")
                || name.equalsIgnoreCase(".git")){
            throw new SourceException(
                    "Invalid repository name in repository URL: '" + name + "'.");
        }
    }

    private void validateRepositoryPath(String path) {
        for (String segment : path.split("/", -1)) {
            if (!PATH_SEGMENT.matcher(segment).matches()
                    || segment.equals(".")
                    || segment.equals("..")) {
                throw new SourceException(
                        "Invalid repository path in repository URL: '" + path + "'.");
            }
        }
    }

    private void validateSubPath(String subPath) {
        if (subPath == null || subPath.isEmpty()) {
            return;
        }

        String firstSegment = subPath.split("/", 2)[0];
        if (UNSUPPORTED_GITHUB_PATHS.contains(firstSegment.toLowerCase(Locale.ROOT))) {
            throw new SourceException(
                    "GitHub /tree, /blob and /commit URLs are not supported. "
                            + "Use a directory path instead.");
        }
    }
}

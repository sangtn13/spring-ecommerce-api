# Contributing to SShop

## Commit Message Convention

This project uses [Conventional Commits](https://www.conventionalcommits.org/) for automatic versioning and changelog generation.

### Format
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types
- **feat**: A new feature (triggers minor version bump)
- **fix**: A bug fix (triggers patch version bump)
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools

### Breaking Changes
Add `BREAKING CHANGE:` in the footer or `!` after the type to trigger major version bump.

### Examples
```
feat: add user authentication
fix: resolve cart calculation bug
feat!: change API response format
docs: update README with setup instructions
chore: update dependencies
```

## Release Process

Releases are automatically created when code is pushed to the `main` branch using semantic-release:

1. **Patch** (1.0.1): `fix:` commits
2. **Minor** (1.1.0): `feat:` commits  
3. **Major** (2.0.0): commits with `BREAKING CHANGE:` or `!`

The CI/CD pipeline will:
1. Run tests
2. Build the application
3. Create a GitHub release with changelog
4. Upload JAR artifacts to the release

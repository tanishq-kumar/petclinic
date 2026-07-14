# VS Code Profiles

This workspace includes two importable VS Code profiles:

- `java-only.code-profile`: enables the Java extensions needed for this Spring/Maven workspace.
- `no-java.code-profile`: a lightweight companion profile without the Java extension set.

Import either profile from VS Code with:

1. `Profiles: Import Profile...`
2. Choose `Select File...`
3. Pick one of the `.code-profile` files in this directory

After importing, open the workspace with a profile from the command line:

```sh
code --profile "Spring Petclinic Java" .
code --profile "Spring Petclinic Non-Java" .
```

VS Code profiles are stored per user, so these files are kept in the repo as portable import definitions.

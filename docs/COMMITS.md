# Verified Strategy refactoring history

The downloadable project contains the actual `.git` directory plus the two tags below.

| Stage | Local Git commit | What it demonstrates |
|---|---|---|
| BEFORE | `a01eb84f178303d1b06697fec411a9f26d4d18b6` | One pricing class with a conditional chain covering three algorithms. |
| AFTER | `1c04bcf323323464d60c5a2a906a7235929a8a19` | Extracted PricingStrategy interface, concrete pricing strategies, runtime context, two selectors, second Strategy family. |

## Local commit links / verified tags

- Before: `git show a01eb84f178303d1b06697fec411a9f26d4d18b6`
- After: `git show 1c04bcf323323464d60c5a2a906a7235929a8a19`
- Before tag: `git switch --detach before-strategy`
- After tag: `git switch --detach after-strategy`

**GitHub links:** The assignment asks for links to the before/after commits. The project has no user-supplied GitHub repository URL, so we cannot provide real public links yet. After pushing to your repository, replace `<YOUR_GITHUB_REPOSITORY_URL>` with the exact URL:

- BEFORE: `<YOUR_GITHUB_REPOSITORY_URL>/commit/a01eb84f178303d1b06697fec411a9f26d4d18b6`
- AFTER: `<YOUR_GITHUB_REPOSITORY_URL>/commit/1c04bcf323323464d60c5a2a906a7235929a8a19`

## Confirmed behavior-preserving refactoring

- The original `PricingBehaviorRegressionTest.java` is **byte-for-byte unchanged** between the two tags (`git diff before-strategy after-strategy -- src/test/java/edu/aitu/parking/PricingBehaviorRegressionTest.java` has no output).
- The before source ran **3 checks, 0 failures** in the offline Java assertion harness.
- The after source ran **32 checks, 0 failures** in the offline Java assertion harness, including the same 3 regression checks.
- The original source compiles with `javac` in both versions.
- The current `SmartParkingDemo` compiles with `javac` and produces an end-to-end console demonstration.
- The JUnit 5 source files use real JUnit imports and annotations. Native `mvn test` could not run in the authoring environment due to missing Maven and inaccessible dependency downloads; run it on your computer before defense.

**Offline harness note:** The authoring-time checker temporarily provided minimal local implementations of JUnit assertion/annotation signatures, invoked each test method through reflection, and reported failures. It verified the Java test logic; it is **not** a substitute for an actual JUnit 5 runtime.

## Publish your GitHub links

1. Create a new **empty** GitHub repository, e.g. `smart-parking-garage` (without adding a README on GitHub).
2. Extract the ZIP, including hidden `.git` directory, then open a terminal inside the project folder.
3. Run `git remote add origin https://github.com/USERNAME/REPOSITORY.git` (replace placeholders).
4. Run `git branch -M main`, `git push -u origin main`, then `git push origin --tags`.
5. Paste the actual before/after URL using the hashes above into this document before submission.

**Do not run `git init` after extraction**: a working Git history is already included.

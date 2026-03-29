
Run the application locally with the standard Spring Boot Maven workflow used by the project.

When adding new build steps or plugins, keep them documented and aligned with the project’s current setup.

---

## Dependencies

- Prefer Spring Boot starters over manually managing many low-level dependencies.
- Add new dependencies only when they clearly support a project requirement.
- Keep the dependency set lean.
- Review the impact of every new library on build size, maintenance, and security.

---

## Security

- Treat all external input as untrusted.
- Validate and sanitize user-provided data.
- Do not log secrets, credentials, or tokens.
- Keep authentication and authorization logic isolated.
- Use role-based access control only where it is needed.
- Review access rules when adding new endpoints or operations.

---

## Documentation

- Keep README and project guidelines aligned with the actual codebase.
- Document important architectural decisions when they affect future work.
- Add concise code comments only when the intent is not obvious from the code itself.
- Prefer self-explanatory code over long comments.

---

## Git and Change Management

- Make changes in small, reviewable steps.
- Keep commits focused on one logical purpose.
- Avoid unrelated refactoring in feature work unless it is necessary.
- Update tests and documentation together with code changes when appropriate.

---

## Working Rule of Thumb

When implementing a feature:

1. Identify the business need.
2. Add or update the service logic.
3. Expose the behavior through the controller if needed.
4. Add validation and error handling.
5. Write or update tests.
6. Keep the codebase consistent with the existing structure.

---

## Project-Specific Guidance

This repository is currently minimal, so prefer:
- simple solutions over layered complexity,
- conventions that match Spring Boot defaults,
- only the packages and abstractions that the codebase actually needs right now.

As the project grows, expand the structure gradually and only where it improves maintainability.
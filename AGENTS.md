# AGENTS.md — Java/Spring Backend

> Purpose: Give Codex consistent and safe instructions for working in a Java/Spring backend repository.  
> Scope: These rules apply to this directory and all subdirectories unless a more specific `AGENTS.md` exists deeper in the tree.

---

## 0. Role

You are an autonomous coding agent working inside this repository.

Always follow these priorities:

1. Preserve existing behavior.
2. Make the smallest safe change that solves the user's request.
3. Prefer readable, maintainable code over clever code.
4. Verify changes with tests, builds, type checks, or focused manual reasoning.
5. If a requirement is ambiguous, state the assumption you made instead of expanding the scope.
6. Be extra conservative with security, authentication, authorization, database schema, and external integrations.
7. Do not modify generated files, build artifacts, lockfiles, or migrations unless clearly required.

When the user asks for implementation, implement it.  
When the user asks for investigation, investigate first and avoid unnecessary edits.  
When the user asks for refactoring, preserve public behavior and API compatibility.

---

## 1. Response Style

Use practical Korean by default unless the repository or user request is in English.

Prefer this final response shape:

```markdown
## Changes

- 

## Reason

- 

## Verification

- 

## Notes

- 
```

When explaining a bug, include:

- Root cause
- Location
- Minimal fix
- Verification method

Prefer directly useful commands and explanations over long theory.

---

## 2. Before Editing

Before changing files, inspect as much as practical:

1. Repository structure
2. Java version
3. Spring Boot version
4. Build tool: Gradle or Maven
5. Test framework: JUnit, Mockito, Spring Boot Test, etc.
6. Data access style: JPA, QueryDSL, MyBatis, jOOQ, JDBC, etc.
7. Package and layer structure
8. Exception handling style
9. Common response format
10. Authentication/authorization style
11. Configuration file layout
12. Existing implementation patterns

Do not invent a new structure based on assumptions. Search for similar code first and follow the local style.

---

## 3. Java/Spring Rules

For Java/Spring code:

- Keep controllers thin.
- Put business logic in service classes.
- Keep persistence logic in repositories, DAOs, mappers, or query classes.
- Prefer constructor injection.
- Avoid field injection.
- Keep DTOs separate from entities.
- Do not expose JPA entities directly from APIs unless the project already intentionally does so.
- Keep transaction boundaries explicit at the service layer.
- Use `@Transactional(readOnly = true)` for read-only service methods when appropriate.
- Keep domain rules out of controllers and DTOs.
- Reuse the project's common exception handling.
- Clearly call out impact when API contracts change.

---

## 4. Package and Layer Structure

Follow the existing package structure first.

Common responsibilities:

- `controller`: HTTP request/response handling
- `service`: use cases and business logic
- `repository`, `dao`, `mapper`: database access
- `domain`, `entity`: domain model and persistence model
- `dto`, `request`, `response`: API input/output models
- `config`: configuration
- `exception`: exceptions and error codes
- `client`, `external`, `infra`: external APIs or infrastructure integration

Before creating a new package, check whether an equivalent package already exists.

---

## 5. Gradle and Build Rules

For Gradle projects:

- Do not add Maven XML configuration.
- Add dependencies using the existing `build.gradle` or `build.gradle.kts` style.
- Do not add unnecessary plugins or large dependencies.
- Follow the project's version management approach.
- Use dependency management, version catalogs, or root build scripts if the project already uses them.
- Do not add a dependency for trivial helpers.

Useful verification commands:

```bash
./gradlew test
./gradlew build
./gradlew check
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat test
gradlew.bat build
```

---

## 6. Configuration Rules

Be conservative with configuration files.

- Do not add secrets to `application.yml`, `application-*.yml`, or `.env`.
- Do not mix production configuration with local configuration.
- Check profile-specific behavior.
- Do not create or modify security-sensitive config files that are intentionally not committed. Prefer examples and documentation.
- Never copy real keys, tokens, passwords, or production database URLs into responses.

Use placeholders for examples:

```yaml
external:
  api-key: ${EXTERNAL_API_KEY}
```

---

## 7. Database Rules

Database changes require extra care.

- Follow existing naming conventions.
- Avoid changes that can destroy production data.
- Explain impact and migration strategy for column deletion, type changes, and constraint changes.
- Consider indexes for new query patterns.
- Make pagination and sorting explicit.
- Watch for N+1 queries, full scans, and unnecessary joins.
- Keep seed/dummy data separate from production migrations.

For SQL:

- Prefer explicit column lists in production code.
- Avoid string-concatenated SQL.
- Always bind user input.
- Check SQL injection risks in dynamic search and sorting.

---

## 8. JPA / Hibernate Rules

When using JPA:

- Entities should focus on persistence mapping and domain state.
- Do not accept request DTOs directly as entities.
- Use bidirectional relationships only when needed.
- Be careful with `fetch = EAGER`.
- For query performance, consider fetch joins, EntityGraph, or DTO projections.
- Be careful when combining collection fetch joins with pagination.
- Use dirty checking intentionally inside transactions.
- Consider persistence context consistency after bulk updates.
- Use `Optional` for repository return types, not entity fields.

When adding repository methods, follow the existing style:

- Spring Data JPA derived queries
- `@Query`
- QueryDSL
- jOOQ
- Custom Repository

If the project uses QueryDSL, prefer it for complex dynamic search.

---

## 9. MyBatis Rules

When using MyBatis:

- Match the Mapper XML `namespace` exactly with the Java mapper/DAO interface path.
- Match statement ids exactly with interface method names.
- Use `#{}` for value binding.
- Use `${}` only when unavoidable, such as dynamic column names or sort directions, and only with whitelist validation.
- Do not use `'%#{value}%'` for LIKE search. Use `concat('%', #{value}, '%')`.
- Follow the existing `resultType` or `resultMap` style.
- Watch for duplicate statement ids causing `Mapped Statements collection already contains key`.
- Check whether XML files are loaded twice from source and build output.

Example:

```xml
<select id="findByName" resultType="Member">
    select id, name, email
    from member
    where name like concat('%', #{name}, '%')
</select>
```

---

## 10. QueryDSL / jOOQ Rules

If QueryDSL or jOOQ is present:

- Prefer type-safe query tools over string concatenation for complex dynamic queries.
- Keep condition-building logic readable.
- Consider count query cost for pagination.
- Allow sorting only by whitelisted fields.
- Follow existing conventions for projection DTOs and API DTOs.
- Do not modify generated query code directly.

---

## 11. API Design Rules

When changing APIs:

- Preserve existing URLs, HTTP methods, status codes, and response shapes.
- Use explicit request and response DTOs.
- Validate input at the boundary.
- Use `@Valid` and validation annotations according to project style.
- Follow the common error response format.
- Do not leak internal exception messages to clients.
- Clearly state whether a contract change is a breaking change.

REST defaults:

- Create: `201 Created`
- Read: `200 OK`
- Update: `200 OK` or `204 No Content`, following existing style
- Delete: `204 No Content` or existing style
- Bad request: `400 Bad Request`
- Authentication failure: `401 Unauthorized`
- Authorization failure: `403 Forbidden`
- Not found: `404 Not Found`

---

## 12. Authentication and Authorization Rules

Be very conservative with authentication and authorization code.

- Do not confuse authentication with authorization.
- Be careful when changing `SecurityConfig`; it can affect the entire API.
- Explain intent and risk when adding public endpoints.
- Follow the existing JWT, OAuth, session, or cookie strategy.
- Do not log tokens, passwords, or authorization headers.
- Do not arbitrarily change refresh token storage or revocation policy.
- Keep provider-specific OAuth logic isolated.
- Do not assume email is always present in social login.

For OAuth/social login:

- Verify provider access tokens or authorization codes on the backend.
- Use provider user id as the stable identity.
- Consider account linking, re-registration after withdrawal, and duplicated email cases.
- Keep provider-specific response DTOs separate.

---

## 13. External API Integration Rules

For external APIs:

- Read existing clients, FeignClients, RestClient, or WebClient code first.
- Use explicit request and response DTOs.
- Check timeout, retry, and fallback policies.
- Convert failure responses to common exceptions.
- Keep provider-specific code isolated.
- Do not log sensitive payloads.
- Keep URLs, keys, and secrets in configuration.
- In tests, mock or stub external APIs instead of calling them directly.

For OpenFeign:

- Check for common Feign configuration.
- Reuse existing error decoders.
- Be careful not to expose tokens through interceptors or logs.

---

## 14. Transaction Rules

Manage transactions explicitly.

- Put `@Transactional` on service methods for write use cases.
- Use `@Transactional(readOnly = true)` for read use cases when appropriate.
- Be careful when mixing external API calls with DB transactions.
- Avoid long transactions.
- Consider commit timing for events, async work, and external calls.
- If concurrency is involved, follow existing patterns for optimistic locks, pessimistic locks, or distributed locks.

---

## 15. Caching and Locking Rules

When adding caching, define:

- Cache key format
- TTL
- Cache miss behavior
- Invalidation conditions
- Whether data is user-specific
- Whether data is sensitive
- Whether stampede protection is needed

For Redis/Redisson:

- Reuse existing Redis configuration.
- Define lock lease time and wait time.
- Define behavior when lock acquisition fails.
- Unlock safely in `finally`.
- If AOP-based caching/locking exists, follow existing annotations and policies.

---

## 16. Exception Handling Rules

Follow the project's existing exception handling style.

- Reuse existing ErrorCode values when possible.
- Add new ErrorCode values only when necessary.
- Return user-safe messages.
- Keep internal debugging details in logs only.
- Avoid meaningless `catch Exception`.
- Do not swallow exceptions silently.
- Preserve causes when wrapping checked exceptions.

Good error responses have:

- Consistent shape
- Correct HTTP status
- Safe message
- Traceable internal logs

---

## 17. Logging Rules

Logs should help debugging without exposing sensitive information.

Never log:

- Passwords
- Access tokens
- Refresh tokens
- Authorization headers
- API keys
- National identifiers or equivalent unique personal identifiers
- Payment data
- Full personal-data payloads
- Production database connection details

Prefer logging:

- Request identifiers
- Internal user ids
- External provider names
- Failure summaries
- Processing time
- Safe state values

---

## 18. Testing Rules

After changes, run the narrowest useful verification first.

Priority:

1. Unit tests for changed classes
2. Related service tests
3. Repository/Mapper tests
4. Controller slice tests
5. Integration tests
6. Full test suite
7. Build
8. Manual verification explanation if commands cannot be run

When writing tests:

- Test business rules with unit tests.
- Test database queries with `@DataJpaTest`, MyBatis tests, Testcontainers, or existing project style.
- Test controller request/response, status, and validation.
- Mock or stub external APIs.
- Fix time, randomness, and external state.

With Mockito:

- Avoid unnecessary stubbing.
- Prefer verifying results over implementation details.
- Do not damage design just to test private methods.

---

## 19. Performance Rules

Prefer evidence over speculation.

Check for:

- DB calls inside loops
- N+1 queries
- Unnecessary eager loading
- Missing indexes
- Large payloads
- Unpaginated list endpoints
- Repeated external API calls
- Synchronous blocking work
- Excessive serialization

Start with simple optimizations.  
Use complex caching, async processing, or distributed locking only when there is a clear bottleneck.

---

## 20. Security Checklist

For security-sensitive changes, check:

- Authentication requirement
- Authorization location
- User input validation
- SQL injection
- XSS
- CSRF
- SSRF
- File upload extension, size, and path limits
- Open redirects
- CORS configuration
- Rate limiting
- Sensitive logging
- Token storage and expiry
- Accidental exposure of admin APIs

Default to least privilege.

---

## 21. File Upload / Download Rules

For file features:

- Do not trust file names.
- Prevent path traversal.
- Validate allowed extensions and MIME types.
- Enforce file size limits.
- Clarify public accessibility.
- Follow the existing storage strategy: presigned URL, cloud storage, or local storage.
- Check `Content-Type` and `Content-Disposition` for downloads.

---

## 22. Async and Scheduler Rules

For async or scheduled jobs, check:

- Duplicate execution risk
- Retry policy
- Transaction boundaries
- Idempotency
- Locking needs
- Multi-instance production deployment
- Logging and monitoring

Schedulers can directly affect production data, so modify them conservatively.

---

## 23. Documentation Rules

Update documentation when changing:

- Run instructions
- Environment variables
- API usage
- Database migrations
- Deployment process
- External API configuration
- Test commands

Docs should be short, accurate, and close to the code.  
Do not repeat obvious implementation details.

---

## 24. Git Rules

Check the working tree before editing when possible.

- Do not overwrite user changes.
- Inspect files with unexpected modifications before editing.
- Do not commit, push, merge, or create PRs unless explicitly asked.
- Keep changes scoped to the requested task.

Suggested commit message style:

```text
type: concise summary
```

Examples:

```text
fix: handle duplicate email registration
feat: add recruit search API
refactor: simplify member service transaction flow
test: add scrap search service tests
```

---

## 25. Review Mode

When reviewing code, prioritize:

1. Correctness
2. Security
3. Data loss risk
4. Breaking changes
5. Concurrency issues
6. Performance regressions
7. Maintainability
8. Style consistency

Use this format for each issue:

```markdown
- Severity: major
- Location: `src/main/java/...`
- Problem:
- Suggested fix:
```

Avoid nitpicks unless the code is otherwise clean.

---

## 26. Debugging Mode

When debugging:

1. Read the exact error message.
2. Narrow the failing boundary.
3. Trace from symptom to root cause.
4. Fix the root cause, not only the symptom.
5. Verify with a related test or minimal reproduction command.

The final response must include why the bug happened.

---

## 27. Refactoring Mode

When refactoring:

- Preserve external behavior.
- Do not change API contracts.
- Do not change database schema.
- Improve names, responsibilities, duplication, and testability.
- Prefer small incremental changes.
- Do not mix refactoring with feature work unless the feature requires a minimal refactor.

---

## 28. Prohibited Actions

Do not do these unless explicitly requested:

- Delete large amounts of code
- Rewrite the entire project
- Change package manager
- Upgrade Spring Boot major version
- Change Java major version
- Make destructive database schema changes
- Change authentication/authorization policy
- Change CI/CD deployment behavior
- Change production configuration
- Rotate secrets
- Modify lockfiles
- Commit, push, merge, or create PRs

Never:

- Print secrets
- Copy production credentials into responses
- Add token logging
- Overwrite user changes
- Modify generated code directly
- Modify build artifacts

---

## 29. Common Verification Commands

Run only commands that fit the project.

Gradle:

```bash
./gradlew test
./gradlew build
./gradlew check
./gradlew bootRun
```

Specific test:

```bash
./gradlew test --tests "com.example.MemberServiceTest"
```

Only for Maven projects:

```bash
./mvnw test
./mvnw package
```

If Docker Compose exists:

```bash
docker compose up -d
docker compose logs -f
docker compose down
```

If verification cannot be run, explain why and provide the exact command the user should run.

---

## 30. User Defaults

Unless the user says otherwise:

- Explain in Korean.
- Prefer Java/Spring examples.
- Prefer Gradle.
- Avoid Maven XML configuration.
- Prefer implementation-focused answers.
- Prefer code that is easy to explain in interviews and team reviews.
- Respect the assumption that security-sensitive `yml` files may not be committed.
- Do not modify generated files or build artifacts.
- When time is short, keep hexagonal structure practical and shallow: follow existing domain/application/adapter boundaries, but avoid adding extra use-case interfaces, adapters, or test scaffolding unless they clearly reduce risk.
- If the user explicitly asks to minimize tests, do not add broad test suites; verify with the narrowest useful build/compile command and explain any remaining manual-test risk.

---

## 31. Final Answer Checklist

Before responding, ensure the final answer includes:

- What changed
- Which files changed
- Why it changed
- How it was verified
- Why verification could not be run, if applicable
- Remaining risks or assumptions
- Commands the user should run next

If no files were changed, clearly say "No files changed."

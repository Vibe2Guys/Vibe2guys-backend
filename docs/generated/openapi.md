# OpenAPI

## Runtime Endpoints

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

## Groups

- `auth`
- `learning`
- `analytics`
- `admin`

Grouped documents are exposed at:

- `/v3/api-docs/auth`
- `/v3/api-docs/learning`
- `/v3/api-docs/analytics`
- `/v3/api-docs/admin`

## Security

- JWT bearer authentication is registered as `bearerAuth`
- protected endpoints require `Authorization: Bearer {accessToken}`

## Notes

- springdoc `3.0.0` is used to match Spring Boot `4.x`
- controller packages are documented from runtime annotations, so code and docs stay close together

# Grocery-E-commerce
Grocery E-commerce website built using Spring and made deployable to a Kubernetes cluster

## Technologies used:
- Spring Boot
- Spring Cloud
- Stripe API
- Auth0
- AWS DynamoDB
- Guava caching
- Zipkin
- Docker
- Kubernetes
- JUnit

## Features:
- [x] Login/Logout with ability to save info to profile
- [x] View all products at a glance or view products by category
- [x] View extended product info page
- [x] Add/Remove products to/from cart with auto-increment quantity for items already existing in the cart
- [x] Place and view orders with full checkout experience and payment handling by Stripe
- [x] Fuzzy search for products
## In progress:
- [ ] Multiple saved addresses for shipping and billing
- [ ] Creation of another project that uses existing Order Service API to create an order fulfillment service that updates status and contents of user orders


![Architecture diagram](design_diagrams/architecture_diagram.png)

![Database ER diagram](design_diagrams/database_ER_diagram.png)

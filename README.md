# Intelligence Query System

A project focused on robust data persistence strategies and modern API design patterns. This repository serves as a reference architecture for developers seeking to implement scalable, maintainable, and secure data storage solutions backed by clean API interfaces.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Usage](#usage)
- [API Examples](#api-examples)
- [Contributing](#contributing)
- [License](#license)

## Overview

This project demonstrates best practices in:
- Data persistence (relational/non-relational databases)
- RESTful (or GraphQL) API design
- Clean architecture patterns
- Error handling, validation, and security

It is suitable as a learning resource, a template for new projects, or a reference for applying advanced software design principles.

## Features

- Modular codebase for easy expansion
- Abstracted data access layers
- Reusable API controllers and routes
- Built-in data validation and error handling
- [Add more specific features based on your implementation]

## Project Structure

```
src/
│
├── controllers/     # API endpoint handlers
├── models/          # Data models/schemas
├── routes/          # API route definitions
├── services/        # Business logic and data access
├── db/              # Database connection and utility code
├── utils/           # Helper functions and utilities
├── app.js           # App entry point (express app/bootstrap)
└── ...              # Additional modules as needed
```

## Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/MoscowDev/Data_Persistence_-_API_Design.git
   cd Data_Persistence_-_API_Design
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```
   or
   ```bash
   yarn
   ```

3. **Configure environment variables:**
   Duplicate `.env.example` as `.env` and update settings for your local environment.

4. **Run migrations (if applicable):**
   ```bash
   npm run migrate
   ```

## Usage

- **Start the development server:**
  ```bash
  npm start
  ```

- **Available Scripts:**
  - `npm start` — Start application
  - `npm run dev` — Start in development/watch mode
  - `npm test` — Run tests

## API Examples

### Profile Classification
Classify a person by name, fetching data from Genderize, Agify, and Nationalize.

```http
POST /api/profiles
Content-Type: application/json

{
  "name": "John"
}
```

### Filtering Profiles
Advanced filtering with pagination and sorting.

```http
GET /api/profiles?gender=male&age_group=adult&sort_by=age&order=desc&page=1&limit=10
```

### Natural Language Search
Search profiles using natural language queries.

```http
GET /api/profiles/search?q=men older than 25 in Nigeria
```

## Natural Language Query Support
The system supports the following keywords:
- **Gender**: female, women, girls, woman, girl, male, men, boys, man, boy
- **Age Groups**: child, kid, children, kids, teenager, teen, teens, adult, adults, senior, seniors, elderly, old
- **Operators**: older than, younger than, above, over, below, under, greater than, less than
- **Probabilities**: probability, confidence (e.g., "gender probability 0.8")
- **Countries**: Country names (e.g., Nigeria, Kenya) or 2-letter codes (NG, KE)

## Sorting Options
- `age`
- `created_at`
- `gender_probability`
- `country_probability`
- `country_id`
- `name`

## Contributing

Contributions are welcome! Please open issues or pull requests for suggestions or improvements.

## License

[MIT](LICENSE)

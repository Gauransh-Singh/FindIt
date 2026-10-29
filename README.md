# FindIt — Smart Campus Lost & Found Platform

A Java 17 / Maven WAR web application designed for **Apache Tomcat 9** using the `javax.servlet` API and MySQL.

## Included features
- User registration and login with salted PBKDF2 password hashing
- Session-based protected dashboard and logout
- Create LOST and FOUND reports with category, colour, location, date and description
- Search and filter reports by report type and text
- Suggested matches based on opposite report type, category, and date proximity
- Submit verification claims on another user's open item
- Personal report list and mark-resolved action
- Admin dashboard, claim approval/rejection, and overview counts
- Responsive shared CSS

## Requirements
- JDK 17
- Apache Tomcat 9.x configured in NetBeans
- MySQL 8.x (or compatible)
- NetBeans with Maven support

## Setup
1. Extract the ZIP.
2. Open the `FindIt` directory in NetBeans as a Maven project.
3. In MySQL Workbench, run `database/findit_db.sql`.
4. Edit `src/main/java/com/findit/util/DBConnection.java` and set your local MySQL username/password.
5. Ensure NetBeans runs this project on **Tomcat 9**.
6. Clean and Build, then Run.
7. Open `http://localhost:8080/FindIt/`.

## Admin account
Register a normal user through the website, then promote that account in MySQL Workbench:

```sql
USE findit_db;
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
```

Log out and log in again to refresh the session role.

## Notes
- The smart match feature is a practical rules-based suggestion (opposite report type + same category, ordered by date proximity), not an AI model.
- Claim approval marks the item resolved and rejects other pending claims for that item.
- This is a student project starter; configure database credentials for your machine and review security/privacy requirements before production deployment.
- Do not put sensitive private details in item descriptions. The admin review page can see claim verification answers.

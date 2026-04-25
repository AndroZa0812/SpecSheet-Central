# [cite_start]Specification and Design Document: Electronic Components Trading System [cite: 1]

[cite_start]**Submitted by**: Natan Targov [cite: 2]

---

## [cite_start]1. Functional Description of the System [cite: 3]

[cite_start]The system is a Full-Stack platform designed for the management and sale of electronic components[cite: 4]. [cite_start]The system provides both a technical and commercial solution[cite: 4]. [cite_start]It operates on a Client-Server model, where the Frontend (Svelte) communicates with the Backend (SpringBoot) via HTTP protocol, and the Backend manages transactions with the database [PostgreSQL](cite: 5).

### [cite_start]A. Customer Interface – Data Retrieval and Display [cite: 6]

[cite_start]In this interface, the system focuses on reflecting the database status to the user in real-time[cite: 7]:

* [cite_start]Upon entering the homepage, the Frontend sends a GET request[cite: 8]. [cite_start]The Backend retrieves data from the Products and Categories tables and passes them as a JSON file to the Front[cite: 8].
* [cite_start]The user sees the most up-to-date products[cite: 9].
* [cite_start]When a customer clicks on a specific product, the Backend executes a Join query between the Products table and the Product_Specs table based on the Product_id[cite: 10]. [cite_start]All technical data is sent to the customer and displayed in a dynamic table[cite: 11].
* [cite_start]Upon confirmation of the shopping cart, a POST request is sent to the Backend, and the server opens a transaction in which it[cite: 12]:
  * [cite_start]Creates a new row in the Orders table[cite: 13].
  * [cite_start]Creates a detailed record in the Order_Items table[cite: 14].
  * [cite_start]Updates the Products table by reducing the `stock_quantity`[cite: 15].

### [cite_start]B. Admin Interface – Data Update and Management [cite: 16]

[cite_start]In this interface, the admin's actions directly affect the integrity and availability of the data[cite: 17]:

* [cite_start]**Inventory management**[cite: 18]:
  * [cite_start]**Adding a product**: The admin fills out a form, and the Backend creates new records in the Products and Product_Specs tables[cite: 19].
  * [cite_start]**Updating inventory**: The admin updates the quantity of an item that arrived at the warehouse[cite: 20]. [cite_start]This action sends a PATCH request that directly updates the `stock_quantity` field in the Products table[cite: 21]. [cite_start]The change is immediately reflected to customers [the product will appear as available again](cite: 22).
* [cite_start]**Category management**: Changing a category name in the Categories table will automatically update in all products linked to it due to the use of a relational DB[cite: 23].
* [cite_start]**Order tracking**: The admin retrieves data from the Orders and Users tables to see who made a purchase, and changes the order status in the relevant table [e.g., updating delivery status from "In Delivery" to "Order Delivered"](cite: 24).

---

## [cite_start]2. Implementation Technologies [cite: 25]

* **Frontend**: Svelte. [cite_start]A convenient framework for client-side development based on JavaScript[cite: 26].
* [cite_start]**Backend**: Spring Boot (Java) based on Maven[cite: 27].
* [cite_start]**Database**: PostgreSQL [A relational database for managing transactions and inventory](cite: 28).
* [cite_start]**Security**: Spring Security + JWT for authentication and authorization [Admin/User](cite: 29).
* [cite_start]**Styling**: CSS[cite: 30].

---

## [cite_start]3. Architecture and System Components [cite: 31]

[cite_start]The system is built on a Separated Client-Server model[cite: 32]:

* [cite_start]**Frontend (Svelte)**: A Single Page Application (SPA) that consumes data via REST API[cite: 33].
* [cite_start]**Backend (Spring Boot)**: Built in layers [Layered Architecture](cite: 34):
  * [cite_start]**Controller**: Exposing Endpoints[cite: 35].
  * [cite_start]**Service**: Business logic [price calculations, inventory validation](cite: 36).
  * [cite_start]**Repository**: Working with the database using JPA[cite: 37].
  * [cite_start]**File Storage**: Storing files (Datasheets and images) on the local server[cite: 38].

---

## [cite_start]4. Description of Screens [cite: 39]

* [cite_start]**Home and Catalog Screen**: Product grid with a side filtering bar (Sidebar) and search in the Header[cite: 40].
* [cite_start]**Product Page**: A clean display that includes an image gallery, a technical specifications table, and a PDF download button[cite: 41].
* [cite_start]**Comparison Screen**: A matrix table for comparing parameters of selected components[cite: 42].
* [cite_start]**Shopping Cart**: A list of items with quantity updates and total sum calculation[cite: 43].
* [cite_start]**Admin Management Screen**: Inventory management tables with visual alerts for shortages [Low Stock](cite: 44).

---

## [cite_start]5. Database Schema [cite: 45]

### [cite_start]Details of Tables and Relationships[cite: 46]

* [cite_start]**Categories**[cite: 47]:
  * [cite_start]**Fields**: `id` (primary key), `name`[cite: 48].
  * [cite_start]**Relationship**: 1 to N with the Products table [one category can contain multiple products](cite: 49).
* [cite_start]**Products**[cite: 50]:
  * [cite_start]**Fields**: `id` (primary key), `stock_quantity`, `price`, `name`, `sku`, `category_id` [foreign key](cite: 51).
  * [cite_start]**Relationship**: 1 to N with Product_Specs [one product has many technical characteristics](cite: 52).
* [cite_start]**Product_Specs (Technical Specifications)**[cite: 53]:
  * [cite_start]**Fields**: `id` (primary key), `product_key` (foreign key), `spec_key` (e.g., Voltage), and `spec_value` [e.g., V5](cite: 54).
  * [cite_start]With this kind of table definition, it is possible to dynamically define the relevant parameters for each component we add without adding extra columns for every new type of parameter[cite: 55].
* [cite_start]**Users**[cite: 56]:
  * [cite_start]**Fields**: `id` (primary key), `role` (User/Admin), `password_hash`, `email`[cite: 57].
  * [cite_start]**Relationship**: 1 to N with Orders [one user can place multiple orders](cite: 58).
* [cite_start]**Orders**[cite: 59]:
  * [cite_start]**Fields**: `id` (primary key), `user_id` (foreign key), `order_date`, `total_amount`[cite: 60].
  * [cite_start]**Relationship**: 1 to N with Order_Items [one order can contain a large number of products](cite: 61).
* [cite_start]**Order_Items (Order Details)**[cite: 62]:
  * [cite_start]**Fields**: `id` (primary key), `order_id` (foreign key), `product_id` (foreign key), `quantity`, and `price_at_purchase`[cite: 63].
  * [cite_start]This table links the order to the products actually purchased [for order 123 there will be several records, each with the quantity and product ID that was part of the order](cite: 64).

---

## [cite_start]6. Page Designs [cite: 65]

* [cite_start]**Main Screen** [cite: 66]
* [cite_start]**Products Screen** [cite: 67]
* [cite_start]**Comparison Screen** [cite: 68]
* [cite_start]**Management Screen** [cite: 69]

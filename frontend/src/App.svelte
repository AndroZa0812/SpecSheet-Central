<script lang="ts">
  import { currentPath } from "./lib/router.js";
  import Route from "./components/Route.svelte";
  import Navbar from "./components/Navbar.svelte";
  import Home from "./routes/Home.svelte";
  import Login from "./routes/Login.svelte";
  import Register from "./routes/Register.svelte";
  import Catalog from "./routes/Catalog.svelte";
  import ProductDetail from "./routes/ProductDetail.svelte";
  import Compare from "./routes/Compare.svelte";
  import Cart from "./routes/Cart.svelte";
  import Admin from "./routes/Admin.svelte";
</script>

<Navbar />
<main>
  <Route path="/" component={Home} />
  <Route path="/login" component={Login} />
  <Route path="/register" component={Register} />
  <Route path="/products" component={Catalog} />
  <Route path="/products/:id" component={ProductDetail} />
  <Route path="/compare" component={Compare} />
  <Route path="/cart" component={Cart} />
  <Route path="/admin" component={Admin} />
</main>

<style>
  main { min-height: calc(100vh - 56px); }
</style>

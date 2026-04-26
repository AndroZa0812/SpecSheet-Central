<script lang="ts">
  import { currentPath } from "$lib/router";
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
  import AdminGuard from "./components/AdminGuard.svelte";
  import { Toaster } from "$lib/components/ui/sonner";
</script>

<Navbar />
<main class="min-h-[calc(100vh-3.5rem)]">
  <Route path="/" component={Home} />
  <Route path="/login" component={Login} />
  <Route path="/register" component={Register} />
  <Route path="/products" component={Catalog} />
  <Route path="/products/:id" component={ProductDetail} />
  <Route path="/compare" component={Compare} />
  <Route path="/cart" component={Cart} />
  <Route path="/admin">
    <AdminGuard>
      <Admin />
    </AdminGuard>
  </Route>
</main>
<Toaster />

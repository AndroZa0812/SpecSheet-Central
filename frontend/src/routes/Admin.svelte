<script lang="ts">
  import api from "$lib/api";
  import { formatPrice, getStockVariant } from "$lib/utils";
  import ProductFormModal from "../components/ProductFormModal.svelte";
  import type { ProductResponse, Category, OrderResponse } from "$lib/types";
  import { Button } from "$lib/components/ui/button";
  import { Input } from "$lib/components/ui/input";
  import { Badge } from "$lib/components/ui/badge";
  import { Card, CardContent, CardHeader, CardTitle } from "$lib/components/ui/card";
  import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
  } from "$lib/components/ui/table";
  import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
  } from "$lib/components/ui/dropdown-menu";
  import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
  } from "$lib/components/ui/select";
  import { Separator } from "$lib/components/ui/separator";
  import { Skeleton } from "$lib/components/ui/skeleton";
  import { Empty } from "$lib/components/ui/empty";
  import { Plus, Search, MoreHorizontal, Pencil, Trash2, Package, ArrowUpDown, DollarSign, AlertTriangle } from "lucide-svelte";
  import { toast } from "svelte-sonner";

  let activeTab = $state<"products" | "orders">("products");
  let products: ProductResponse[] = $state([]);
  let categories: Category[] = $state([]);
  let orders: OrderResponse[] = $state([]);
  let loading = $state(true);
  let showModal = $state(false);
  let editingProduct: ProductResponse | null = $state(null);
  let searchQuery = $state("");

  let totalProducts = $derived(products.length);
  let totalStockUnits = $derived(products.reduce((sum, p) => sum + p.stockQuantity, 0));
  let inventoryValue = $derived(products.reduce((sum, p) => sum + p.price * p.stockQuantity, 0));
  let lowStockItems = $derived(products.filter((p) => p.stockQuantity <= (p.lowStockThreshold ?? 10)).length);

  let filteredProducts = $derived(
    searchQuery
      ? products.filter(
          (p) =>
            p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            p.sku.toLowerCase().includes(searchQuery.toLowerCase()),
        )
      : products,
  );

  $effect(() => {
    loadData();
  });

  async function loadData() {
    loading = true;
    try {
      if (activeTab === "products") {
        const [prodRes, catRes] = await Promise.all([
          api.get<ProductResponse[]>("/products"),
          api.get<Category[]>("/categories"),
        ]);
        products = prodRes.data;
        categories = catRes.data;
      } else if (activeTab === "orders") {
        const res = await api.get<OrderResponse[]>("/admin/orders");
        orders = res.data;
      }
    } catch (err) {
      console.error("Failed to load admin data", err);
    } finally {
      loading = false;
    }
  }

  function openCreate() {
    editingProduct = null;
    showModal = true;
  }

  function openEdit(product: ProductResponse) {
    editingProduct = product;
    showModal = true;
  }

  function handleSave() {
    showModal = false;
    loadData();
  }

  async function handleDelete(id: number) {
    try {
      await api.delete(`/admin/products/${id}`);
      toast.success("Product deleted");
      loadData();
    } catch (err) {
      toast.error("Failed to delete product");
    }
  }

  async function updateStock(id: number, quantity: number) {
    try {
      await api.patch(`/admin/products/${id}/stock`, null, { params: { quantity } });
      toast.success("Stock updated");
      loadData();
    } catch (err) {
      toast.error("Failed to update stock");
    }
  }

  async function updateOrderStatus(id: number, status: string) {
    try {
      await api.patch(`/admin/orders/${id}/status`, null, { params: { status } });
      toast.success("Order status updated");
      loadData();
    } catch (err) {
      toast.error("Failed to update order status");
    }
  }

  function getStatusVariant(status: string): "default" | "secondary" | "destructive" {
    switch (status) {
      case "DELIVERED": return "default";
      case "IN_DELIVERY": return "secondary";
      case "CANCELLED": return "destructive";
      default: return "secondary";
    }
  }
</script>

<div class="px-4 py-6">
  <div class="mb-6 flex items-center justify-between">
    <h1 class="text-2xl font-bold">Admin Panel</h1>
  </div>

  <div class="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
    <Card>
      <CardContent class="flex items-center gap-4 p-6">
        <div class="flex size-10 items-center justify-center rounded-lg bg-primary/10">
          <Package class="size-5 text-primary" />
        </div>
        <div>
          <p class="text-sm text-muted-foreground">Total Products</p>
          {#if loading}
            <Skeleton class="mt-1 h-7 w-12" />
          {:else}
            <p class="text-2xl font-bold">{totalProducts}</p>
          {/if}
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="flex items-center gap-4 p-6">
        <div class="flex size-10 items-center justify-center rounded-lg bg-primary/10">
          <ArrowUpDown class="size-5 text-primary" />
        </div>
        <div>
          <p class="text-sm text-muted-foreground">Stock Units</p>
          {#if loading}
            <Skeleton class="mt-1 h-7 w-16" />
          {:else}
            <p class="text-2xl font-bold">{totalStockUnits.toLocaleString()}</p>
          {/if}
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="flex items-center gap-4 p-6">
        <div class="flex size-10 items-center justify-center rounded-lg bg-primary/10">
          <DollarSign class="size-5 text-primary" />
        </div>
        <div>
          <p class="text-sm text-muted-foreground">Inventory Value</p>
          {#if loading}
            <Skeleton class="mt-1 h-7 w-20" />
          {:else}
            <p class="text-2xl font-bold">${inventoryValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
          {/if}
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="flex items-center gap-4 p-6">
        <div class="flex size-10 items-center justify-center rounded-lg bg-warning/10">
          <AlertTriangle class="size-5 text-warning" />
        </div>
        <div>
          <p class="text-sm text-muted-foreground">Low Stock Items</p>
          {#if loading}
            <Skeleton class="mt-1 h-7 w-12" />
          {:else}
            <p class="text-2xl font-bold">{lowStockItems}</p>
          {/if}
        </div>
      </CardContent>
    </Card>
  </div>

  <div class="mb-4 flex items-center gap-2">
    <button
      class="rounded-md px-4 py-2 text-sm font-medium {activeTab === 'products' ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:text-foreground'}"
      onclick={() => { activeTab = "products"; loadData(); }}
    >
      Products
    </button>
    <button
      class="rounded-md px-4 py-2 text-sm font-medium {activeTab === 'orders' ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:text-foreground'}"
      onclick={() => { activeTab = "orders"; loadData(); }}
    >
      Orders
    </button>
  </div>

  {#if activeTab === "products"}
    <div class="mb-4 flex items-center justify-between gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input bind:value={searchQuery} class="pl-9" placeholder="Search products..." />
      </div>
      <Button onclick={openCreate}>
        <Plus class="mr-2 size-4" />
        Add Product
      </Button>
    </div>

    {#if loading}
      <div class="flex flex-col gap-3">
        {#each { length: 5 } as _, i}
          <Skeleton class="h-12 w-full" />
        {/each}
      </div>
    {:else if filteredProducts.length === 0}
      <Empty title="No products found" description={searchQuery ? "Try a different search term." : "Add your first product to get started."} />
    {:else}
      <div class="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>SKU</TableHead>
              <TableHead>Price</TableHead>
              <TableHead>Stock</TableHead>
              <TableHead>Category</TableHead>
              <TableHead class="w-24">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {#each filteredProducts as product}
              <TableRow>
                <TableCell class="font-medium">{product.name}</TableCell>
                <TableCell class="text-muted-foreground">{product.sku}</TableCell>
                <TableCell>{formatPrice(product.price)}</TableCell>
                <TableCell>
                  <Badge variant={getStockVariant(product.stockQuantity, product.lowStockThreshold)}>
                    {product.stockQuantity}
                  </Badge>
                </TableCell>
                <TableCell>{product.categoryName}</TableCell>
                <TableCell>
                  <DropdownMenu>
                    <DropdownMenuTrigger>
                      {#snippet children()}
                        <Button variant="ghost" size="icon" class="size-8">
                          <MoreHorizontal class="size-4" />
                        </Button>
                      {/snippet}
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onclick={() => openEdit(product)}>
                        <Pencil class="mr-2 size-4" />
                        Edit
                      </DropdownMenuItem>
                      <DropdownMenuItem class="text-destructive" onclick={() => handleDelete(product.id)}>
                        <Trash2 class="mr-2 size-4" />
                        Delete
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            {/each}
          </TableBody>
        </Table>
      </div>
    {/if}
  {/if}

  {#if activeTab === "orders"}
    {#if loading}
      <div class="flex flex-col gap-3">
        {#each { length: 5 } as _, i}
          <Skeleton class="h-12 w-full" />
        {/each}
      </div>
    {:else if orders.length === 0}
      <Empty title="No orders found" description="Orders will appear here once customers place them." />
    {:else}
      <div class="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>User</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Status</TableHead>
              <TableHead class="w-40">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {#each orders as order}
              <TableRow>
                <TableCell class="font-medium">#{order.id}</TableCell>
                <TableCell>{order.userEmail}</TableCell>
                <TableCell>{new Date(order.orderDate).toLocaleDateString()}</TableCell>
                <TableCell>${order.totalAmount.toFixed(2)}</TableCell>
                <TableCell>
                  <Badge variant={getStatusVariant(order.status)}>{order.status}</Badge>
                </TableCell>
                <TableCell>
                  <Select value={[order.status]} onValueChange={(v: string[]) => updateOrderStatus(order.id, v[0])}>
                    <SelectTrigger class="h-8">
                      <span>{order.status}</span>
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PENDING">PENDING</SelectItem>
                      <SelectItem value="IN_DELIVERY">IN_DELIVERY</SelectItem>
                      <SelectItem value="DELIVERED">DELIVERED</SelectItem>
                      <SelectItem value="CANCELLED">CANCELLED</SelectItem>
                    </SelectContent>
                  </Select>
                </TableCell>
              </TableRow>
            {/each}
          </TableBody>
        </Table>
      </div>
    {/if}
  {/if}

  {#if showModal}
    <ProductFormModal
      product={editingProduct}
      categories={categories}
      onsave={handleSave}
      onclose={() => showModal = false}
    />
  {/if}
</div>

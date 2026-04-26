<script lang="ts">
  import { onMount } from "svelte";
  import api from "$lib/api";
  import { cart } from "$lib/stores";
  import { formatPrice, getStockStatus, getStockVariant } from "$lib/utils";
  import type { ProductResponse, Category } from "$lib/types";
  import { Button } from "$lib/components/ui/button";
  import { Input } from "$lib/components/ui/input";
  import { Badge } from "$lib/components/ui/badge";
  import { Card, CardContent } from "$lib/components/ui/card";
  import { Separator } from "$lib/components/ui/separator";
  import { Skeleton } from "$lib/components/ui/skeleton";
  import { Empty } from "$lib/components/ui/empty";
  import {
    X,
    Plus,
    Search,
    ShoppingCart,
    Star,
    Scale,
    AlertCircle,
  } from "lucide-svelte";
  import { toast } from "svelte-sonner";

  let selectedIds: number[] = $state([]);
  let products: ProductResponse[] = $state([]);
  let allProducts: ProductResponse[] = $state([]);
  let categories: Category[] = $state([]);
  let loading = $state(true);
  let productsLoading = $state(false);
  let searchQuery = $state("");
  let showPicker = $state(false);

  const MAX_COMPARE = 4;

  let allSpecKeys = $derived([...new Set(products.flatMap((p) => Object.keys(p.specs || {})))].sort());

  let filteredAll = $derived(() => {
    const q = searchQuery.toLowerCase().trim();
    return allProducts.filter(
      (p) =>
        !selectedIds.includes(p.id) &&
        (p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q) || p.manufacturer?.toLowerCase().includes(q)),
    );
  });

  onMount(async () => {
    try {
      const [catRes, prodRes] = await Promise.all([
        api.get<Category[]>("/categories"),
        api.get<ProductResponse[]>("/products"),
      ]);
      categories = catRes.data;
      allProducts = prodRes.data;
    } catch (err) {
      console.error("Failed to load data", err);
    } finally {
      loading = false;
    }
  });

  async function loadProducts() {
    if (selectedIds.length === 0) {
      products = [];
      return;
    }
    productsLoading = true;
    try {
      const results = await Promise.all(
        selectedIds.map((id) =>
          api.get<ProductResponse>(`/products/${id}`).catch(() => null),
        ),
      );
      products = results.filter(Boolean).map((r) => r!.data);
    } catch (err) {
      console.error("Failed to load products for comparison", err);
    } finally {
      productsLoading = false;
    }
  }

  function addProduct(id: number) {
    if (selectedIds.length >= MAX_COMPARE) {
      toast.error(`You can compare up to ${MAX_COMPARE} products`);
      return;
    }
    if (!selectedIds.includes(id)) {
      selectedIds = [...selectedIds, id];
      loadProducts();
    }
  }

  function removeProduct(id: number) {
    selectedIds = selectedIds.filter((s) => s !== id);
    loadProducts();
  }

  function handleAddFromCard(product: ProductResponse) {
    addProduct(product.id);
    searchQuery = "";
    showPicker = false;
  }

  function renderStars(rating: number) {
    const full = Math.floor(rating);
    const hasHalf = rating - full >= 0.5;
    const empty = 5 - full - (hasHalf ? 1 : 0);
    return { full, hasHalf, empty };
  }
</script>

<div class="px-4 py-6">
  <div class="mb-6 flex items-center gap-3">
    <Scale class="size-6 text-primary" />
    <h1 class="text-2xl font-bold">Compare Products</h1>
  </div>

  <!-- Selected products chips -->
  {#if selectedIds.length > 0}
    <div class="mb-6 flex flex-wrap items-center gap-2">
      {#each products as product}
        <div class="flex items-center gap-2 rounded-full border bg-muted/50 px-3 py-1.5 text-sm">
          {#if product.imageUrl}
            <img src={product.imageUrl} alt="" class="size-6 rounded object-contain" />
          {:else}
            <div class="size-6 rounded bg-muted" />
          {/if}
          <span class="max-w-[180px] truncate font-medium">{product.name}</span>
          <button
            type="button"
            class="ml-1 rounded-full p-0.5 hover:bg-muted-foreground/20"
            onclick={() => removeProduct(product.id)}
          >
            <X class="size-3.5 text-muted-foreground" />
          </button>
        </div>
      {/each}
      {#if selectedIds.length < MAX_COMPARE}
        <Button variant="outline" size="sm" onclick={() => { showPicker = true; searchQuery = ""; }}>
          <Plus class="mr-1 size-3.5" />
          Add Product
        </Button>
      {:else}
        <span class="text-xs text-muted-foreground">Max {MAX_COMPARE} products</span>
      {/if}
    </div>
  {/if}

  <!-- Product picker overlay / inline -->
  {#if showPicker || selectedIds.length === 0}
    <Card class="mb-6">
      <CardContent class="p-4">
        <div class="mb-3 flex items-center gap-2">
          <Search class="size-4 text-muted-foreground" />
          <Input
            bind:value={searchQuery}
            placeholder="Search products to compare..."
            class="flex-1"
          />
          {#if selectedIds.length > 0}
            <Button variant="ghost" size="sm" onclick={() => showPicker = false}>Done</Button>
          {/if}
        </div>

        {#if loading}
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
            {#each { length: 4 } as _, i}
              <Skeleton class="h-24 w-full rounded-lg" />
            {/each}
          </div>
        {:else if filteredAll().length === 0}
          <p class="py-6 text-center text-sm text-muted-foreground">
            {searchQuery.trim() ? "No products match your search." : "All products are already selected."}
          </p>
        {:else}
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
            {#each filteredAll().slice(0, 12) as product}
              <button
                type="button"
                class="flex items-center gap-3 rounded-lg border p-3 text-left transition-all hover:border-primary/50 hover:shadow-sm"
                onclick={() => handleAddFromCard(product)}
              >
                <div class="flex size-12 shrink-0 items-center justify-center rounded-md bg-muted">
                  {#if product.imageUrl}
                    <img src={product.imageUrl} alt="" class="max-h-10 max-w-10 object-contain" />
                  {:else}
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="text-muted-foreground"><rect width="18" height="18" x="3" y="3" rx="2" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" /></svg>
                  {/if}
                </div>
                <div class="min-w-0">
                  <p class="truncate text-sm font-medium">{product.name}</p>
                  <p class="text-xs text-muted-foreground">{formatPrice(product.price)}</p>
                </div>
              </button>
            {/each}
          </div>
          {#if filteredAll().length > 12}
            <p class="mt-2 text-center text-xs text-muted-foreground">{filteredAll().length - 12} more products — refine your search</p>
          {/if}
        {/if}
      </CardContent>
    </Card>
  {/if}

  <!-- Comparison table -->
  {#if productsLoading}
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {#each { length: Math.max(selectedIds.length, 2) } as _, i}
        <Skeleton class="h-80 w-full rounded-lg" />
      {/each}
    </div>
  {:else if products.length === 0}
    <Empty
      title="No products to compare"
      description="Search and select products above to compare them side by side. You can compare up to 4 products at once."
    />
  {:else}
    <div class="overflow-x-auto rounded-lg border">
      <table class="w-full min-w-[600px]">
        <thead>
          <tr class="border-b bg-muted/50">
            <th class="w-40 px-4 py-3 text-left text-sm font-medium text-muted-foreground">Feature</th>
            {#each products as product}
              <th class="min-w-[200px] px-4 py-3 text-left">
                <div class="flex flex-col gap-2">
                  <div class="flex items-center justify-between">
                    <span class="font-medium">{product.name}</span>
                    <Button variant="ghost" size="icon" class="size-6 shrink-0" onclick={() => removeProduct(product.id)}>
                      <X class="size-4" />
                    </Button>
                  </div>
                </div>
              </th>
            {/each}
          </tr>
        </thead>
        <tbody>
          <!-- Image row -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium text-muted-foreground"></td>
            {#each products as product}
              <td class="px-4 py-3">
                <div class="flex aspect-square w-full max-w-[160px] items-center justify-center rounded-lg bg-muted">
                  {#if product.imageUrl}
                    <img src={product.imageUrl} alt={product.name} class="max-h-full max-w-full object-contain p-2" />
                  {:else}
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="text-muted-foreground"><rect width="18" height="18" x="3" y="3" rx="2" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" /></svg>
                  {/if}
                </div>
              </td>
            {/each}
          </tr>

          <!-- Price -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium">Price</td>
            {#each products as product}
              <td class="px-4 py-3 text-lg font-semibold">{formatPrice(product.price)}</td>
            {/each}
          </tr>

          <!-- Rating -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium">Rating</td>
            {#each products as product}
              <td class="px-4 py-3">
                {#if product.rating != null && product.reviewCount > 0}
                  {@const stars = renderStars(product.rating)}
                  <div class="flex items-center gap-1">
                    {#each { length: stars.full } as _, i}
                      <Star class="size-3.5 fill-amber-400 text-amber-400" />
                    {/each}
                    {#if stars.hasHalf}
                      <Star class="size-3.5 fill-amber-400 text-amber-400" />
                    {/if}
                    {#each { length: stars.empty } as _, i}
                      <Star class="size-3.5 text-muted-foreground" />
                    {/each}
                    <span class="ml-1 text-xs text-muted-foreground">{product.rating.toFixed(1)} ({product.reviewCount})</span>
                  </div>
                {:else}
                  <span class="text-sm text-muted-foreground">No ratings</span>
                {/if}
              </td>
            {/each}
          </tr>

          <!-- SKU -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium">SKU</td>
            {#each products as product}
              <td class="px-4 py-3 text-sm font-mono text-muted-foreground">{product.sku}</td>
            {/each}
          </tr>

          <!-- Manufacturer -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium">Manufacturer</td>
            {#each products as product}
              <td class="px-4 py-3 text-sm">{product.manufacturer || "-"}</td>
            {/each}
          </tr>

          <!-- Category -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium">Category</td>
            {#each products as product}
              <td class="px-4 py-3 text-sm">
                <Badge variant="secondary">{product.categoryName}</Badge>
              </td>
            {/each}
          </tr>

          <!-- Stock -->
          <tr class="border-b">
            <td class="px-4 py-3 text-sm font-medium">Stock</td>
            {#each products as product}
              <td class="px-4 py-3">
                <Badge variant={getStockVariant(product.stockQuantity, product.lowStockThreshold)}>
                  {getStockStatus(product.stockQuantity, product.lowStockThreshold)} ({product.stockQuantity})
                </Badge>
              </td>
            {/each}
          </tr>

          <!-- Specs -->
          {#each allSpecKeys as key}
            <tr class="border-b">
              <td class="px-4 py-3 text-sm font-medium">{key}</td>
              {#each products as product}
                <td class="px-4 py-3 text-sm">{product.specs?.[key] || "-"}</td>
              {/each}
            </tr>
          {/each}

          <!-- Actions -->
          <tr>
            <td class="px-4 py-3 text-sm font-medium"></td>
            {#each products as product}
              <td class="px-4 py-3">
                <Button
                  size="sm"
                  onclick={() => cart.add(product as any)}
                  disabled={product.stockQuantity === 0}
                >
                  <ShoppingCart class="mr-1.5 size-3.5" />
                  Add to Cart
                </Button>
              </td>
            {/each}
          </tr>
        </tbody>
      </table>
    </div>
  {/if}
</div>

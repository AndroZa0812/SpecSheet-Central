<script lang="ts">
  import { onMount } from "svelte";
  import { currentPath } from "$lib/router";
  import api from "$lib/api";
  import { cart } from "$lib/stores";
  import ProductCard from "../components/ProductCard.svelte";
  import { formatPrice } from "$lib/utils";
  import type { ProductResponse, Category } from "$lib/types";
  import { Button } from "$lib/components/ui/button";
  import { Input } from "$lib/components/ui/input";
  import { Label } from "$lib/components/ui/label";
  import { Slider } from "$lib/components/ui/slider";
  import { Separator } from "$lib/components/ui/separator";
  import { Skeleton } from "$lib/components/ui/skeleton";
  import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
    SheetTrigger,
  } from "$lib/components/ui/sheet";
  import { Empty } from "$lib/components/ui/empty";
  import { Filter, Search } from "lucide-svelte";

  let products: ProductResponse[] = $state([]);
  let categories: Category[] = $state([]);
  let loading = $state(true);
  let search = $state("");
  let selectedCategories: number[] = $state([]);
  let selectedManufacturers: string[] = $state([]);
  let priceRange: number[] = $state([0, 1000]);
  let sheetOpen = $state(false);

  const manufacturers = $derived([...new Set(products.map((p) => p.manufacturer).filter(Boolean) as string[])].sort());

  function parseQueryParams(): { search?: string; categoryId?: number; manufacturer?: string; minPrice?: number; maxPrice?: number } {
    const path = $currentPath;
    const queryStr = path.split("?")[1];
    if (!queryStr) return {};
    const params: Record<string, string> = {};
    queryStr.split("&").forEach((pair) => {
      const [k, v] = pair.split("=");
      if (k) params[decodeURIComponent(k)] = decodeURIComponent(v || "");
    });
    return {
      search: params.search || undefined,
      categoryId: params.categoryId ? parseInt(params.categoryId) : undefined,
      manufacturer: params.manufacturer || undefined,
      minPrice: params.minPrice ? parseFloat(params.minPrice) : undefined,
      maxPrice: params.maxPrice ? parseFloat(params.maxPrice) : undefined,
    };
  }

  async function fetchProducts() {
    loading = true;
    try {
      const params: Record<string, string> = {};
      if (search) params.search = search;
      if (selectedCategories.length === 1) {
        params.categoryId = String(selectedCategories[0]);
      } else if (selectedCategories.length > 1) {
        params.categoryId = selectedCategories.join(",");
      }
      if (selectedManufacturers.length > 0) params.manufacturer = selectedManufacturers.join(",");
      params.minPrice = String(priceRange[0]);
      params.maxPrice = String(priceRange[1]);
      const [prodRes, catRes] = await Promise.all([
        api.get<ProductResponse[]>("/products", { params }),
        api.get<Category[]>("/categories"),
      ]);
      products = prodRes.data;
      categories = catRes.data;
    } catch (err) {
      console.error("Failed to load products", err);
    } finally {
      loading = false;
    }
  }

  let lastQuery = $state("");

  function applyUrlParams() {
    const qp = parseQueryParams();
    const queryKey = JSON.stringify(qp);
    if (queryKey === lastQuery) return false;
    lastQuery = queryKey;
    if (qp.search) search = qp.search;
    if (qp.categoryId) selectedCategories = [qp.categoryId];
    if (qp.manufacturer) selectedManufacturers = [qp.manufacturer];
    if (qp.minPrice != null) priceRange[0] = qp.minPrice;
    if (qp.maxPrice != null) priceRange[1] = qp.maxPrice;
    return true;
  }

  onMount(() => {
    applyUrlParams();
    fetchProducts();

    // React to URL changes (client-side navigation)
    const unsub = currentPath.subscribe(() => {
      if (applyUrlParams()) {
        fetchProducts();
      }
    });
    return unsub;
  });

  function toggleCategory(catId: number, checked: boolean) {
    selectedCategories = checked
      ? [...selectedCategories, catId]
      : selectedCategories.filter((id) => id !== catId);
    fetchProducts();
  }

  function toggleManufacturer(mfr: string, checked: boolean) {
    selectedManufacturers = checked
      ? [...selectedManufacturers, mfr]
      : selectedManufacturers.filter((m) => m !== mfr);
    fetchProducts();
  }
</script>

{#snippet FiltersContent()}
  <div class="flex flex-col gap-4">
    <div class="flex flex-col gap-2">
      <Label>Search</Label>
      <div class="relative">
        <Search class="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input bind:value={search} oninput={fetchProducts} class="pl-9" placeholder="Search..." />
      </div>
    </div>

    <Separator />

    <div class="flex flex-col gap-2">
      <Label>Price Range</Label>
      <div class="flex items-center gap-2">
        <span class="text-sm text-muted-foreground">${priceRange[0]}</span>
        <Slider bind:value={priceRange} min={0} max={1000} step={10} />
        <span class="text-sm text-muted-foreground">${priceRange[1]}</span>
      </div>
    </div>

    <Separator />

    <div class="flex flex-col gap-3">
      <Label>Categories</Label>
      {#each categories as cat}
        <label class="flex items-center gap-2.5 cursor-pointer group">
          <span class="relative flex size-4 shrink-0 items-center justify-center rounded border border-muted-foreground/50 bg-background transition-colors group-hover:border-primary">
            <input
              type="checkbox"
              class="peer absolute inset-0 opacity-0 cursor-pointer"
              checked={selectedCategories.includes(cat.id)}
              onchange={(e) => toggleCategory(cat.id, (e.target as HTMLInputElement).checked)}
            />
            <svg class="size-3 text-primary opacity-0 peer-checked:opacity-100 transition-opacity pointer-events-none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
          <span class="text-sm font-normal">{cat.name}</span>
        </label>
      {/each}
    </div>

    <Separator />

    <div class="flex flex-col gap-3">
      <Label>Manufacturers</Label>
      {#each manufacturers as mfr}
        <label class="flex items-center gap-2.5 cursor-pointer group">
          <span class="relative flex size-4 shrink-0 items-center justify-center rounded border border-muted-foreground/50 bg-background transition-colors group-hover:border-primary">
            <input
              type="checkbox"
              class="peer absolute inset-0 opacity-0 cursor-pointer"
              checked={selectedManufacturers.includes(mfr)}
              onchange={(e) => toggleManufacturer(mfr, (e.target as HTMLInputElement).checked)}
            />
            <svg class="size-3 text-primary opacity-0 peer-checked:opacity-100 transition-opacity pointer-events-none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
          <span class="text-sm font-normal">{mfr}</span>
        </label>
      {/each}
    </div>
  </div>
{/snippet}

<div class="flex gap-6 px-4 py-6">
  <aside class="hidden w-64 shrink-0 md:block">
    {@render FiltersContent()}
  </aside>

  <div class="flex-1">
    <div class="mb-4 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">Product Catalog</h1>
        {#if !loading}
          <p class="text-sm text-muted-foreground">{products.length} products found</p>
        {/if}
      </div>
      <div class="md:hidden">
        <Sheet bind:open={sheetOpen}>
        <SheetTrigger>
          {#snippet children()}
            <Button variant="outline" size="sm">
              <Filter class="mr-2 size-4" />
              Filters
            </Button>
          {/snippet}
        </SheetTrigger>
        <SheetContent side="left">
          <SheetHeader>
            <SheetTitle>Filters</SheetTitle>
          </SheetHeader>
          <div class="mt-4">
            {@render FiltersContent()}
          </div>
        </SheetContent>
      </Sheet>
      </div>
    </div>

    {#if loading}
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {#each { length: 8 } as _, i}
          <div class="flex flex-col gap-3">
            <Skeleton class="aspect-square rounded-lg" />
            <Skeleton class="h-4 w-3/4" />
            <Skeleton class="h-4 w-1/2" />
            <Skeleton class="h-6 w-1/3" />
          </div>
        {/each}
      </div>
    {:else if products.length === 0}
      <Empty title="No products found" description="Try adjusting your filters or search terms." />
    {:else}
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {#each products as product}
          <ProductCard {product} />
        {/each}
      </div>
    {/if}
  </div>
</div>

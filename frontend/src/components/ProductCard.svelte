<script lang="ts">
  import { cart } from "$lib/stores";
  import { navigate } from "$lib/router";
  import { formatPrice, getStockStatus, getStockVariant, cn } from "$lib/utils";
  import type { ProductResponse } from "$lib/types";
  import { Badge } from "$lib/components/ui/badge";
  import { Button } from "$lib/components/ui/button";
  import { Card, CardContent, CardFooter } from "$lib/components/ui/card";
  import { Star, StarHalf } from "lucide-svelte";

  interface Props {
    product: ProductResponse;
  }

  let { product }: Props = $props();

  let stockStatus = $derived(getStockStatus(product.stockQuantity, product.lowStockThreshold));
  let stockVariant = $derived(getStockVariant(product.stockQuantity, product.lowStockThreshold));

  function handleAddToCart(e: Event) {
    e.stopPropagation();
    e.preventDefault();
    cart.add(product as any);
  }

  function handleNavigate() {
    navigate(`/products/${product.id}`);
  }

  function renderStars(rating: number) {
    const full = Math.floor(rating);
    const hasHalf = rating - full >= 0.5;
    const empty = 5 - full - (hasHalf ? 1 : 0);
    return { full, hasHalf, empty };
  }
</script>

<Card class="group cursor-pointer overflow-hidden transition-shadow hover:shadow-md">
  <div onclick={handleNavigate} onkeydown={(e) => e.key === "Enter" && handleNavigate()} role="button" tabindex="0">
    <div class="relative aspect-square bg-muted">
      {#if product.imageUrl}
        <img src={product.imageUrl} alt={product.name} class="h-full w-full object-contain p-4" />
      {:else}
        <div class="flex h-full items-center justify-center text-muted-foreground">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" /></svg>
        </div>
      {/if}
      <Badge class="absolute left-2 top-2" variant="secondary">{product.categoryName}</Badge>
    </div>

    <CardContent class="p-4">
      <h3 class="font-medium leading-tight">{product.name}</h3>
      {#if product.manufacturer}
        <p class="mt-1 text-sm text-muted-foreground">{product.manufacturer}</p>
      {/if}

      <div class="mt-2 flex items-center gap-1">
        {#if product.rating != null && product.reviewCount > 0}
          {@const stars = renderStars(product.rating)}
          {#each { length: stars.full } as _, i}
            <Star class="size-3.5 fill-amber-400 text-amber-400" />
          {/each}
          {#if stars.hasHalf}
            <StarHalf class="size-3.5 fill-amber-400 text-amber-400" />
          {/if}
          {#each { length: stars.empty } as _, i}
            <Star class="size-3.5 text-muted-foreground" />
          {/each}
          <span class="ml-1 text-xs text-muted-foreground">({product.reviewCount})</span>
        {:else}
          <span class="text-xs text-muted-foreground">No ratings yet</span>
        {/if}
      </div>
    </CardContent>
  </div>

  <CardFooter class="flex items-center justify-between gap-2 p-4 pt-0">
    <span class="text-lg font-semibold">{formatPrice(product.price)}</span>
    <Badge variant={stockVariant} class="text-xs">{stockStatus}</Badge>
  </CardFooter>

  <div class="px-4 pb-4">
    <Button onclick={handleAddToCart} class="w-full" disabled={product.stockQuantity === 0}>
      Add to Cart
    </Button>
  </div>
</Card>

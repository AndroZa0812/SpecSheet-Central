<script lang="ts">
  import { cart, auth } from "$lib/stores";
  import { formatPrice } from "$lib/utils";
  import { navigate } from "$lib/router";
  import api from "$lib/api";
  import type { OrderResponse } from "$lib/types";
  import { Button } from "$lib/components/ui/button";
  import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "$lib/components/ui/card";
  import { Separator } from "$lib/components/ui/separator";
  import { Alert, AlertDescription } from "$lib/components/ui/alert";
  import { Empty } from "$lib/components/ui/empty";
  import { Minus, Plus, Trash2, Loader2, AlertCircle, ShoppingCart, ArrowRight } from "lucide-svelte";
  import { toast } from "svelte-sonner";

  let error = $state("");
  let ordering = $state(false);
  let orderSuccess = $state(false);

  let total = $derived($cart.reduce((sum: number, item) => sum + item.price * item.quantity, 0));

  async function placeOrder() {
    if (!$auth) {
      navigate("/login");
      return;
    }
    ordering = true;
    error = "";
    try {
      const items = $cart.map((i) => ({ productId: i.id, quantity: i.quantity }));
      await api.post<OrderResponse>("/orders", { items });
      cart.clear();
      orderSuccess = true;
      toast.success("Order placed successfully");
    } catch (err) {
      error = (err as any).response?.data?.message || "Order failed. Please try again.";
      toast.error("Order failed");
    } finally {
      ordering = false;
    }
  }
</script>

<div class="mx-auto max-w-4xl px-4 py-6">
  <h1 class="mb-6 text-2xl font-bold">Shopping Cart</h1>

  {#if orderSuccess}
    <Card>
      <CardContent class="flex flex-col items-center gap-4 py-12">
        <div class="flex size-16 items-center justify-center rounded-full bg-success/10">
          <ShoppingCart class="size-8 text-success" />
        </div>
        <h2 class="text-xl font-semibold">Order placed successfully!</h2>
        <p class="text-muted-foreground">Your order has been confirmed and is being processed.</p>
        <Button onclick={() => navigate("/products")}>Continue Shopping</Button>
      </CardContent>
    </Card>
  {:else if $cart.length === 0}
    <Empty title="Your cart is empty" description="Browse our catalog to find the components you need.">
      <Button onclick={() => navigate("/products")}>
        Browse Products
        <ArrowRight class="ml-2 size-4" />
      </Button>
    </Empty>
  {:else}
    <div class="flex flex-col gap-6 lg:flex-row">
      <div class="flex-1">
        <div class="rounded-lg border">
          {#each $cart as item (item.id)}
            <div class="flex items-center gap-4 p-4 {item !== $cart[$cart.length - 1] ? 'border-b' : ''}">
              <div class="flex size-16 shrink-0 items-center justify-center rounded-md bg-muted">
                {#if item.imageUrl}
                  <img src={item.imageUrl} alt={item.name} class="max-h-full max-w-full object-contain p-2" />
                {:else}
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="text-muted-foreground"><rect width="18" height="18" x="3" y="3" rx="2" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" /></svg>
                {/if}
              </div>

              <div class="flex-1">
                <h3 class="font-medium">{item.name}</h3>
                <p class="text-sm text-muted-foreground">{item.sku}</p>
                <p class="text-sm font-medium">{formatPrice(item.price)}</p>
              </div>

              <div class="flex items-center gap-1">
                <Button variant="outline" size="icon" class="size-8" onclick={() => cart.updateQuantity(item.id, item.quantity - 1)}>
                  <Minus class="size-3.5" />
                </Button>
                <span class="w-8 text-center text-sm">{item.quantity}</span>
                <Button variant="outline" size="icon" class="size-8" onclick={() => cart.updateQuantity(item.id, item.quantity + 1)}>
                  <Plus class="size-3.5" />
                </Button>
              </div>

              <div class="w-24 text-right font-medium">{formatPrice(item.price * item.quantity)}</div>

              <Button variant="ghost" size="icon" class="size-8" onclick={() => cart.remove(item.id)}>
                <Trash2 class="size-4 text-muted-foreground" />
              </Button>
            </div>
          {/each}
        </div>
      </div>

      <div class="w-full lg:w-80">
        <Card>
          <CardHeader>
            <CardTitle>Order Summary</CardTitle>
          </CardHeader>
          <CardContent class="flex flex-col gap-4">
            <div class="flex justify-between text-sm">
              <span class="text-muted-foreground">Subtotal</span>
              <span>{formatPrice(total)}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-muted-foreground">Shipping</span>
              <span>Free</span>
            </div>
            <Separator />
            <div class="flex justify-between text-lg font-semibold">
              <span>Total</span>
              <span>{formatPrice(total)}</span>
            </div>
            {#if error}
              <Alert variant="destructive">
                <AlertCircle class="size-4" />
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            {/if}
          </CardContent>
          <CardFooter>
            <Button class="w-full" onclick={placeOrder} disabled={ordering}>
              {#if ordering}
                <Loader2 class="mr-2 size-4 animate-spin" />
              {/if}
              Place Order
            </Button>
          </CardFooter>
        </Card>
      </div>
    </div>
  {/if}
</div>

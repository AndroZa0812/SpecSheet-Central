<script lang="ts">
  import { onMount } from "svelte";
  import Link from "../components/Link.svelte";
  import api from "$lib/api";
  import type { Category } from "$lib/types";
  import { Button } from "$lib/components/ui/button";
  import { Card, CardContent } from "$lib/components/ui/card";
  import { Skeleton } from "$lib/components/ui/skeleton";
  import { Cpu, Thermometer, Lightbulb, Cog, Monitor, Wrench, Zap, Package } from "lucide-svelte";

  const categoryIcons: Record<string, typeof Cpu> = {
    "Microcontrollers": Cpu,
    "Sensors": Thermometer,
    "LEDs": Lightbulb,
    "Motors": Cog,
    "Displays": Monitor,
    "Tools & Accessories": Wrench,
    "Single Board Computers": Zap,
    "Passive Components": Package,
  };

  const categoryColors: Record<string, string> = {
    "Microcontrollers": "text-blue-500",
    "Sensors": "text-green-500",
    "LEDs": "text-amber-500",
    "Motors": "text-orange-500",
    "Displays": "text-purple-500",
    "Tools & Accessories": "text-slate-500",
    "Single Board Computers": "text-cyan-500",
    "Passive Components": "text-rose-500",
  };

  let categories: Category[] = $state([]);
  let loading = $state(true);

  onMount(async () => {
    try {
      const res = await api.get<Category[]>("/categories");
      categories = res.data;
    } catch (err) {
      console.error("Failed to load categories", err);
    } finally {
      loading = false;
    }
  });

  function getIcon(name: string) {
    return categoryIcons[name] ?? Package;
  }

  function getColor(name: string) {
    return categoryColors[name] ?? "text-muted-foreground";
  }
</script>

<div>
  <section class="bg-gradient-to-b from-primary/5 to-background">
    <div class="mx-auto max-w-7xl px-4 py-16 sm:py-24">
      <div class="max-w-2xl">
        <span class="mb-4 inline-block rounded-full bg-primary/10 px-3 py-1 text-sm font-medium text-primary">New Arrivals Weekly</span>
        <h1 class="text-4xl font-bold tracking-tight sm:text-5xl">Electronic Components for Your Projects</h1>
        <p class="mt-4 text-lg text-muted-foreground">Browse quality components from Arduino boards to sensors, LEDs, and more. Fast shipping and expert support.</p>
        <div class="mt-8 flex gap-3">
          <Link to="/products">
            <Button size="lg">Shop Now</Button>
          </Link>
          <Link to="/compare">
            <Button variant="outline" size="lg">Compare Parts</Button>
          </Link>
        </div>
      </div>
    </div>
  </section>

  <section class="mx-auto max-w-7xl px-4 py-12">
    <h2 class="mb-6 text-xl font-semibold">Shop by Category</h2>

    {#if loading}
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {#each { length: 4 } as _, i}
          <Card>
            <CardContent class="flex flex-col items-center gap-3 p-6 sm:p-8">
              <Skeleton class="size-12 rounded-lg sm:size-14" />
              <Skeleton class="h-4 w-24" />
            </CardContent>
          </Card>
        {/each}
      </div>
    {:else}
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {#each categories as category}
          {@const Icon = getIcon(category.name)}
          {@const color = getColor(category.name)}
          <Link to={`/products?categoryId=${category.id}`} class="block">
            <Card class="h-full cursor-pointer transition-all hover:border-primary/50 hover:shadow-md">
              <CardContent class="flex flex-col items-center gap-3 p-6 sm:p-8">
                <div class="flex size-12 items-center justify-center rounded-lg bg-muted sm:size-14">
                  <Icon class={"size-6 sm:size-7 " + color} />
                </div>
                <span class="text-sm font-medium text-center">{category.name}</span>
              </CardContent>
            </Card>
          </Link>
        {/each}
      </div>
    {/if}
  </section>
</div>

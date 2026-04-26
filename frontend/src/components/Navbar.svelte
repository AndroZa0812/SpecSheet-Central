<script lang="ts">
  import { navigate } from "$lib/router";
  import Link from "./Link.svelte";
  import { auth, cart } from "$lib/stores";
  import { Button } from "$lib/components/ui/button";
  import { Input } from "$lib/components/ui/input";
  import { Avatar, AvatarFallback } from "$lib/components/ui/avatar";
  import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
  } from "$lib/components/ui/dropdown-menu";
  import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
    SheetTrigger,
  } from "$lib/components/ui/sheet";
  import { Separator } from "$lib/components/ui/separator";
  import { Search, ShoppingCart, Menu, User, LogOut, Package, Settings, LayoutGrid, ArrowLeftRight } from "lucide-svelte";

  let cartCount = $derived($cart.reduce((sum: number, i) => sum + i.quantity, 0));
  let searchQuery = $state("");
  let mobileOpen = $state(false);

  function handleLogout() {
    auth.logout();
    navigate("/");
  }

  function handleSearch(e: KeyboardEvent) {
    if (e.key === "Enter" && searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
      searchQuery = "";
    }
  }

  function getInitials(email: string): string {
    return email.charAt(0).toUpperCase();
  }
</script>

<header class="sticky top-0 z-50 border-b border-border bg-background">
  <div class="mx-auto flex h-14 max-w-7xl items-center gap-4 px-4">
    <div class="flex items-center gap-2">
      <Sheet bind:open={mobileOpen}>
        <SheetTrigger>
          {#snippet children()}
            <Button variant="ghost" size="icon" class="md:hidden">
              <Menu class="size-5" />
            </Button>
          {/snippet}
        </SheetTrigger>
        <SheetContent side="left">
          <SheetHeader>
            <SheetTitle>Navigation</SheetTitle>
          </SheetHeader>
          <nav class="mt-4 flex flex-col gap-1">
            <Link to="/" class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium hover:bg-muted" onclick={() => mobileOpen = false}>
              <LayoutGrid class="size-4" />
              Home
            </Link>
            <Link to="/products" class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium hover:bg-muted" onclick={() => mobileOpen = false}>
              <Package class="size-4" />
              Products
            </Link>
            <Link to="/compare" class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium hover:bg-muted" onclick={() => mobileOpen = false}>
              <ArrowLeftRight class="size-4" />
              Compare
            </Link>
            {#if $auth?.role === "ADMIN"}
              <Link to="/admin" class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium hover:bg-muted" onclick={() => mobileOpen = false}>
                <Settings class="size-4" />
                Admin
              </Link>
            {/if}
          </nav>
        </SheetContent>
      </Sheet>

      <Link to="/" class="flex items-center gap-2">
        <div class="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-semibold text-primary-foreground">S</div>
        <span class="hidden font-semibold sm:inline-block">SpecSheet Central</span>
      </Link>
    </div>

    <nav class="hidden items-center gap-1 md:flex">
      <Link to="/" class="rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">Home</Link>
      <Link to="/products" class="rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">Products</Link>
      <Link to="/compare" class="rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">Compare</Link>
      {#if $auth?.role === "ADMIN"}
        <Link to="/admin" class="rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">Admin</Link>
      {/if}
    </nav>

    <div class="flex flex-1 items-center justify-end gap-2">
      <div class="relative hidden w-full max-w-xs md:block">
        <Search class="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          type="search"
          placeholder="Search components..."
          bind:value={searchQuery}
          onkeydown={handleSearch}
          class="pl-9"
        />
      </div>

      <Link to="/cart" class="relative">
        <Button variant="ghost" size="icon">
          <ShoppingCart class="size-5" />
          {#if cartCount > 0}
            <span class="absolute -right-0.5 -top-0.5 flex size-4 items-center justify-center rounded-full bg-primary text-[10px] font-medium text-primary-foreground">{cartCount}</span>
          {/if}
        </Button>
      </Link>

      {#if $auth}
        <DropdownMenu>
          <DropdownMenuTrigger>
            {#snippet children()}
              <Button variant="ghost" size="icon" class="rounded-full">
                <Avatar class="size-7">
                  <AvatarFallback class="bg-primary text-xs text-primary-foreground">{getInitials($auth.email)}</AvatarFallback>
                </Avatar>
              </Button>
            {/snippet}
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <div class="px-2 py-1.5 text-sm font-medium">{$auth.email}</div>
            <div class="px-2 py-1 text-xs text-muted-foreground">{$auth.role}</div>
            <DropdownMenuSeparator />
            <DropdownMenuItem onclick={handleLogout}>
              <LogOut class="mr-2 size-4" />
              Logout
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      {:else}
        <div class="hidden items-center gap-1 md:flex">
          <Link to="/login">
            <Button variant="ghost" size="sm">Login</Button>
          </Link>
          <Link to="/register">
            <Button size="sm">Register</Button>
          </Link>
        </div>
      {/if}
    </div>
  </div>

  <div class="border-t border-border px-4 py-2 md:hidden">
    <div class="relative">
      <Search class="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
      <Input
        type="search"
        placeholder="Search components..."
        bind:value={searchQuery}
        onkeydown={handleSearch}
        class="pl-9"
      />
    </div>
  </div>
</header>

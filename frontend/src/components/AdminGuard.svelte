<script lang="ts">
  import { auth } from "$lib/stores";
  import { navigate } from "$lib/router";

  import type { Snippet } from "svelte";

  let { children }: { children: Snippet } = $props();

  let isAdmin = $derived($auth?.role === "ADMIN");

  $effect(() => {
    if (!isAdmin) {
      navigate("/");
    }
  });
</script>

{#if isAdmin}
  {@render children()}
{/if}
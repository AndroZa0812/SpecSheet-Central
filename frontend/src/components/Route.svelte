<script lang="ts">
  import { currentPath, match } from "$lib/router";
  import type { RouteParams } from "$lib/types";

  import type { Snippet } from "svelte";

  let { path, component: Component, children, ...rest }: { path: string; component?: any; children?: Snippet; [key: string]: any } = $props();
  let matched: RouteParams | null = $derived(match(path, $currentPath.split("?")[0]));
</script>

{#if matched}
  {#if children}
    {@render children()}
  {:else}
    <Component params={matched} {...rest} />
  {/if}
{/if}

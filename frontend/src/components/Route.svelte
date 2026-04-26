<script lang="ts">
  import { currentPath, match } from "$lib/router";
  import type { RouteParams } from "$lib/types";

  let { path, component: Component, ...rest }: { path: string; component: any; [key: string]: any } = $props();
  let matched: RouteParams | null = $derived(match(path, $currentPath.split("?")[0]));
</script>

{#if matched}
  <Component params={matched} {...rest} />
{/if}

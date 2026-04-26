<script lang="ts">
  import { link } from "$lib/router";
  import { cn } from "$lib/utils";
  import type { HTMLAnchorAttributes } from "svelte/elements";

  interface Props {
    to: string;
    class?: string;
    children?: import("svelte").Snippet;
  }

  let { to, class: className, children, ...rest }: Props & Omit<HTMLAnchorAttributes, "class"> = $props();
</script>

<a {...link(to)} class={cn("inline-flex items-center gap-2", className)} {...rest}>
  {@render children?.()}
</a>

<script lang="ts">
  import { link } from "../lib/router.js";

  let { to, children, ...rest }: { to: string; children?: any; [key: string]: any } = $props();
</script>

<a {...link(to)} {...rest}>
  {@render children?.()}
</a>

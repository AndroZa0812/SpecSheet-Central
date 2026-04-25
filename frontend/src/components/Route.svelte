<script>
  import { currentPath, match } from '../lib/router.js'

  let { path, component: Component, ...rest } = $props()
  let matched = $derived(match(path, $currentPath.split('?')[0]))
</script>

{#if matched}
  <Component params={matched} {...rest} />
{/if}

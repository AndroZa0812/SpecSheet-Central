<script>
  import { link } from '../lib/router.js'

  let { to, children, ...rest } = $props()
</script>

<a {...link(to)} {...rest}>
  {@render children?.()}
</a>

<script>
  import { Link } from 'svelte-routing'
</script>

<main>
  <h1>SpecSheet Central</h1>
  <p>Your source for electronic components and datasheets.</p>
  <Link to="/catalog">Browse Catalog</Link>
</main>

<style>
  main {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
    gap: 1rem;
  }
  h1 { font-size: 2.5rem; color: var(--primary); }
  a {
    padding: 0.75rem 1.5rem;
    background: var(--primary);
    color: white;
    text-decoration: none;
    border-radius: 0.5rem;
  }
</style>

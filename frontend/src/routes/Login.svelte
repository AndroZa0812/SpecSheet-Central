<script lang="ts">
  import api from "../lib/api.js";
  import { auth } from "../lib/stores.js";
  import { navigate } from "../lib/router.js";
  import type { AuthResponse } from "../lib/types.js";

  let email = $state("");
  let password = $state("");
  let error = $state("");

  async function handleSubmit(e: Event) {
    e.preventDefault();
    try {
      const res = await api.post<AuthResponse>("/auth/login", { email, password });
      auth.login({ email: res.data.email, role: res.data.role }, res.data.token);
      navigate("/");
    } catch (e) {
      error = (e as any).response?.data?.message || "Login failed";
    }
  }
</script>

<div class="auth-page">
  <div class="auth-card">
    <h1>Login</h1>
    <form onsubmit={handleSubmit}>
      {#if error}<p class="error">{error}</p>{/if}
      <input type="email" placeholder="Email" bind:value={email} required />
      <input type="password" placeholder="Password" bind:value={password} required />
      <button type="submit">Login</button>
    </form>
  </div>
</div>

<style>
  .auth-page { display: flex; justify-content: center; align-items: center; min-height: 80vh; }
  .auth-card { background: white; padding: 2rem; border-radius: 0.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
  h1 { margin-bottom: 1.5rem; }
  form { display: flex; flex-direction: column; gap: 1rem; }
  input { padding: 0.75rem; border: 1px solid var(--gray-200); border-radius: 0.375rem; font-size: 1rem; }
  button { padding: 0.75rem; background: var(--primary); color: white; border: none; border-radius: 0.375rem; font-size: 1rem; cursor: pointer; }
  button:hover { background: var(--primary-dark); }
  .error { color: var(--danger); font-size: 0.9rem; }
</style>

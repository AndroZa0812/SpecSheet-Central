<script lang="ts">
  import api from "$lib/api";
  import { auth } from "$lib/stores";
  import { navigate } from "$lib/router";
  import type { AuthResponse } from "$lib/types";
  import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "$lib/components/ui/card";
  import { Input } from "$lib/components/ui/input";
  import { Label } from "$lib/components/ui/label";
  import { Button } from "$lib/components/ui/button";
  import { Alert, AlertDescription } from "$lib/components/ui/alert";
  import { Separator } from "$lib/components/ui/separator";
  import Link from "../components/Link.svelte";
  import { AlertCircle, Loader2 } from "lucide-svelte";

  let email = $state("");
  let password = $state("");
  let confirmPassword = $state("");
  let error = $state("");
  let loading = $state(false);

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = "";
    if (password !== confirmPassword) {
      error = "Passwords do not match";
      return;
    }
    loading = true;
    try {
      const res = await api.post<AuthResponse>("/auth/register", { email, password });
      auth.login({ email: res.data.email, role: res.data.role }, res.data.token);
      navigate("/");
    } catch (err) {
      error = (err as any).response?.data?.message || "Registration failed";
    } finally {
      loading = false;
    }
  }
</script>

<div class="flex min-h-[calc(100vh-3.5rem)] items-center justify-center px-4 py-12">
  <Card class="w-full max-w-sm">
    <CardHeader>
      <CardTitle>Create an account</CardTitle>
      <CardDescription>Enter your details to get started.</CardDescription>
    </CardHeader>
    <form onsubmit={handleSubmit}>
      <CardContent class="flex flex-col gap-4">
        {#if error}
          <Alert variant="destructive">
            <AlertCircle class="size-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        {/if}
        <div class="flex flex-col gap-2">
          <Label for="email">Email</Label>
          <Input id="email" type="email" bind:value={email} required autocomplete="email" />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="password">Password</Label>
          <Input id="password" type="password" bind:value={password} required autocomplete="new-password" />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="confirm">Confirm Password</Label>
          <Input id="confirm" type="password" bind:value={confirmPassword} required autocomplete="new-password" />
        </div>
      </CardContent>
      <CardFooter class="flex flex-col gap-4">
        <Button type="submit" class="w-full" disabled={loading}>
          {#if loading}
            <Loader2 class="mr-2 size-4 animate-spin" />
          {/if}
          Register
        </Button>
        <Separator />
        <p class="text-center text-sm text-muted-foreground">
          Already have an account?
          <Link to="/login" class="font-medium text-primary underline underline-offset-4">Login</Link>
        </p>
      </CardFooter>
    </form>
  </Card>
</div>

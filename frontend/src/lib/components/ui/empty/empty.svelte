<script lang="ts">
	import { cn, type WithElementRef } from "$lib/utils.js";
	import type { HTMLAttributes } from "svelte/elements";

	let {
		ref = $bindable(null),
		class: className,
		title,
		description,
		children,
		...restProps
	}: WithElementRef<HTMLAttributes<HTMLDivElement>> & { title?: string; description?: string } = $props();
</script>

<div
	bind:this={ref}
	data-slot="empty"
	class={cn(
		"gap-4 rounded-xl border-dashed p-6 flex w-full min-w-0 flex-1 flex-col items-center justify-center text-center text-balance",
		className
	)}
	{...restProps}
>
	{#if title}
		<h3 class="font-medium">{title}</h3>
	{/if}
	{#if description}
		<p class="text-sm text-muted-foreground">{description}</p>
	{/if}
	{@render children?.()}
</div>

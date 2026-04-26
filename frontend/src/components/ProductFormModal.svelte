<script lang="ts">
  import { onMount } from "svelte";
  import api from "$lib/api";
  import type { ProductResponse, Category } from "$lib/types";
  import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
  } from "$lib/components/ui/dialog";
  import { Button } from "$lib/components/ui/button";
  import { Input } from "$lib/components/ui/input";
  import { Label } from "$lib/components/ui/label";
  import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
  } from "$lib/components/ui/select";
  import { Separator } from "$lib/components/ui/separator";
  import { Plus, X } from "lucide-svelte";
  import { toast } from "svelte-sonner";

  interface Props {
    product?: ProductResponse | null;
    categories?: Category[];
    onsave: () => void;
    onclose: () => void;
  }

  let { product = null, categories = [], onsave, onclose }: Props = $props();

  let form = $state({
    name: "",
    sku: "",
    price: "",
    stockQuantity: "",
    lowStockThreshold: "10",
    categoryId: "",
    manufacturer: "",
    imageUrl: "",
    datasheetUrl: "",
    description: "",
    specs: [] as { key: string; value: string }[],
  });

  let saving = $state(false);

  onMount(() => {
    if (product) {
      form = {
        name: product.name,
        sku: product.sku,
        price: String(product.price),
        stockQuantity: String(product.stockQuantity),
        lowStockThreshold: String(product.lowStockThreshold ?? 10),
        categoryId: String(product.categoryId || ""),
        manufacturer: product.manufacturer || "",
        imageUrl: product.imageUrl || "",
        datasheetUrl: product.datasheetUrl || "",
        description: product.description || "",
        specs: Object.entries(product.specs || {}).map(([k, v]) => ({ key: k, value: v })),
      };
    }
    if (form.specs.length === 0) {
      form.specs = [{ key: "", value: "" }];
    }
  });

  function addSpec() {
    form.specs = [...form.specs, { key: "", value: "" }];
  }

  function removeSpec(index: number) {
    form.specs = form.specs.filter((_, i) => i !== index);
  }

  async function handleSubmit(e: Event) {
    e.preventDefault();
    saving = true;
    const data = {
      name: form.name,
      sku: form.sku,
      price: parseFloat(form.price),
      stockQuantity: parseInt(form.stockQuantity),
      lowStockThreshold: parseInt(form.lowStockThreshold),
      categoryId: parseInt(form.categoryId),
      manufacturer: form.manufacturer,
      imageUrl: form.imageUrl,
      datasheetUrl: form.datasheetUrl,
      description: form.description,
      specs: Object.fromEntries(
        form.specs.filter((s) => s.key).map((s) => [s.key, s.value]),
      ),
    };

    try {
      if (product) {
        await api.put(`/products/${product.id}`, data);
        toast.success("Product updated");
      } else {
        await api.post("/products", data);
        toast.success("Product created");
      }
      onsave();
    } catch (err) {
      toast.error((err as any).response?.data?.message || "Save failed");
    } finally {
      saving = false;
    }
  }
</script>

<Dialog open onOpenChange={(open) => !open && onclose()}>
  <DialogContent class="max-h-[90vh] max-w-2xl overflow-y-auto">
    <DialogHeader>
      <DialogTitle>{product ? "Edit Product" : "Add Product"}</DialogTitle>
      <DialogDescription>
        {product ? "Update the product details below." : "Fill in the details to add a new product."}
      </DialogDescription>
    </DialogHeader>

    <form onsubmit={handleSubmit} class="flex flex-col gap-4">
      <div class="grid grid-cols-2 gap-4">
        <div class="flex flex-col gap-2">
          <Label for="name">Name</Label>
          <Input id="name" bind:value={form.name} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="sku">SKU</Label>
          <Input id="sku" bind:value={form.sku} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="price">Price</Label>
          <Input id="price" type="number" step="0.01" bind:value={form.price} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="stock">Stock Quantity</Label>
          <Input id="stock" type="number" bind:value={form.stockQuantity} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="threshold">Low Stock Threshold</Label>
          <Input id="threshold" type="number" bind:value={form.lowStockThreshold} />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="category">Category</Label>
          <Select value={form.categoryId ? [form.categoryId] : []} onValueChange={(v: string[]) => form.categoryId = v[0] ?? ""}>
            <SelectTrigger>
              {#if form.categoryId}
                <span>{categories.find(c => String(c.id) === form.categoryId)?.name ?? "Select a category"}</span>
              {:else}
                <span class="text-muted-foreground">Select a category</span>
              {/if}
            </SelectTrigger>
            <SelectContent>
              {#each categories as cat}
                <SelectItem value={String(cat.id)}>{cat.name}</SelectItem>
              {/each}
            </SelectContent>
          </Select>
        </div>
        <div class="flex flex-col gap-2">
          <Label for="manufacturer">Manufacturer</Label>
          <Input id="manufacturer" bind:value={form.manufacturer} />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="image">Image URL</Label>
          <Input id="image" bind:value={form.imageUrl} />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="datasheet">Datasheet URL</Label>
          <Input id="datasheet" bind:value={form.datasheetUrl} />
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <Label for="description">Description</Label>
        <Input id="description" bind:value={form.description} />
      </div>

      <Separator />

      <div class="flex flex-col gap-3">
        <div class="flex items-center justify-between">
          <Label>Technical Specifications</Label>
          <Button type="button" variant="outline" size="sm" onclick={addSpec}>
            <Plus class="mr-1 size-3.5" />
            Add Spec
          </Button>
        </div>
        {#each form.specs as spec, i}
          <div class="flex items-center gap-2">
            <Input placeholder="Key" bind:value={spec.key} class="flex-1" />
            <Input placeholder="Value" bind:value={spec.value} class="flex-1" />
            <Button type="button" variant="ghost" size="icon" onclick={() => removeSpec(i)}>
              <X class="size-4" />
            </Button>
          </div>
        {/each}
      </div>

      <DialogFooter>
        <Button type="button" variant="outline" onclick={onclose}>Cancel</Button>
        <Button type="submit" disabled={saving}>
          {saving ? "Saving..." : "Save Product"}
        </Button>
      </DialogFooter>
    </form>
  </DialogContent>
</Dialog>

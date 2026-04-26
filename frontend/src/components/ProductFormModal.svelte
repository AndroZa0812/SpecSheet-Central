<script lang="ts">
  import { onMount } from "svelte";
  import api, { uploadDatasheet } from "$lib/api";
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
  import { Plus, X, FileText, Link } from "lucide-svelte";
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
    costPrice: "",
    stockQuantity: "",
    lowStockThreshold: "10",
    categoryId: "",
    manufacturer: "",
    imageUrl: "",
    description: "",
    specs: [] as { key: string; value: string }[],
  });

  let datasheetFile: File | null = $state(null);
  let datasheetUrlInput = $state("");
  let clearDatasheet = $state(false);
  let saving = $state(false);

  onMount(() => {
    if (product) {
      form = {
        name: product.name,
        sku: product.sku,
        price: String(product.price),
        costPrice: product.costPrice != null ? String(product.costPrice) : "",
        stockQuantity: String(product.stockQuantity),
        lowStockThreshold: String(product.lowStockThreshold ?? 10),
        categoryId: String(product.categoryId || ""),
        manufacturer: product.manufacturer || "",
        imageUrl: product.imageUrl || "",
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

  function handleFileChange(e: Event) {
    const target = e.target as HTMLInputElement;
    datasheetFile = target.files?.[0] ?? null;
    if (datasheetFile) {
      datasheetUrlInput = "";
      clearDatasheet = false;
    }
  }

  function handleClearDatasheet() {
    clearDatasheet = true;
    datasheetFile = null;
    datasheetUrlInput = "";
  }

  function handleUndoClear() {
    clearDatasheet = false;
  }

  async function handleSubmit(e: Event) {
    e.preventDefault();
    saving = true;
    const data: any = {
      name: form.name,
      sku: form.sku,
      price: parseFloat(form.price),
      costPrice: form.costPrice ? parseFloat(form.costPrice) : null,
      stockQuantity: parseInt(form.stockQuantity),
      lowStockThreshold: parseInt(form.lowStockThreshold),
      manufacturer: form.manufacturer,
      imageUrl: form.imageUrl,
      description: form.description,
      specs: Object.fromEntries(
        form.specs.filter((s) => s.key).map((s) => [s.key, s.value]),
      ),
    };

    if (form.categoryId) {
      data.categoryId = parseInt(form.categoryId);
    }

    try {
      let savedProduct: ProductResponse;
      if (product) {
        const res = await api.put<ProductResponse>(`/admin/products/${product.id}`, data);
        savedProduct = res.data;
      } else {
        const res = await api.post<ProductResponse>("/admin/products", data);
        savedProduct = res.data;
      }

      if (datasheetFile || datasheetUrlInput || clearDatasheet) {
        await uploadDatasheet(
          savedProduct.id,
          datasheetFile,
          datasheetUrlInput || null,
          clearDatasheet,
        );
      }

      toast.success(product ? "Product updated" : "Product created");
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
          <Label for="costPrice">Cost Price</Label>
          <Input id="costPrice" type="number" step="0.01" bind:value={form.costPrice} placeholder="What you paid" />
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
          <Select type="single" value={form.categoryId} onValueChange={(v: string) => form.categoryId = v}>
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
      </div>

      <div class="flex flex-col gap-2">
        <Label for="description">Description</Label>
        <Input id="description" bind:value={form.description} />
      </div>

      <Separator />

      <div class="flex flex-col gap-2">
        <Label>Datasheet</Label>
        {#if product?.datasheetFilename && !clearDatasheet}
          <div class="flex items-center gap-2 rounded-md border px-3 py-2 text-sm">
            <FileText class="size-4 text-muted-foreground" />
            <span class="flex-1 truncate">{product.datasheetFilename}</span>
            <Button type="button" variant="ghost" size="sm" class="text-destructive" onclick={handleClearDatasheet}>
              <X class="mr-1 size-3.5" />
              Remove
            </Button>
          </div>
        {:else if clearDatasheet}
          <div class="flex items-center gap-2 rounded-md border border-destructive/50 px-3 py-2 text-sm text-destructive">
            <span class="flex-1">Datasheet will be removed on save</span>
            <Button type="button" variant="ghost" size="sm" onclick={handleUndoClear}>
              Undo
            </Button>
          </div>
        {:else}
          <div class="flex flex-col gap-2">
            <Input id="datasheet-file" type="file" accept=".pdf" onchange={handleFileChange} />
            {#if datasheetFile}
              <p class="text-xs text-muted-foreground">Selected: {datasheetFile.name}</p>
            {:else}
              <p class="text-xs text-muted-foreground">Upload a PDF file</p>
            {/if}
            <div class="flex items-center gap-2">
              <div class="flex-1 border-t"></div>
              <span class="text-xs text-muted-foreground">or</span>
              <div class="flex-1 border-t"></div>
            </div>
            <div class="flex items-center gap-2">
              <Link class="size-4 text-muted-foreground shrink-0" />
              <Input id="datasheet-url" placeholder="https://example.com/datasheet.pdf" bind:value={datasheetUrlInput} />
            </div>
            <p class="text-xs text-muted-foreground">The PDF will be fetched and stored locally.</p>
          </div>
        {/if}
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
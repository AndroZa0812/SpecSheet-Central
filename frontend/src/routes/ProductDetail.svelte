<script lang="ts">
  import { onMount } from "svelte";
  import api, { getReviews, createReview, updateReview, deleteReview } from "$lib/api";
  import { cart, auth } from "$lib/stores";
  import { navigate } from "$lib/router";
  import { formatPrice, getStockStatus, getStockVariant } from "$lib/utils";
  import type { ProductResponse, RouteParams, Review } from "$lib/types";
  import { Button } from "$lib/components/ui/button";
  import { Badge } from "$lib/components/ui/badge";
  import { Card, CardContent } from "$lib/components/ui/card";
  import { Skeleton } from "$lib/components/ui/skeleton";
  import { Tabs, TabsContent, TabsList, TabsTrigger } from "$lib/components/ui/tabs";
  import { Separator } from "$lib/components/ui/separator";
  import { Textarea } from "$lib/components/ui/textarea";
  import { Label } from "$lib/components/ui/label";
  import { Alert, AlertDescription } from "$lib/components/ui/alert";
  import { Avatar, AvatarFallback } from "$lib/components/ui/avatar";
  import { Star, ArrowLeft, ShoppingCart, ExternalLink, Trash2, Loader2 } from "lucide-svelte";
  import { toast } from "svelte-sonner";

  let { params }: { params: RouteParams } = $props();
  let product: ProductResponse | null = $state(null);
  let loading = $state(true);
  let reviews: Review[] = $state([]);
  let reviewsLoading = $state(false);
  let newReviewRating = $state(5);
  let newReviewComment = $state("");
  let submittingReview = $state(false);
  let editingReview = $state(false);

  let myReview = $derived(reviews.find((r) => r.userEmail === $auth?.email));

  let stockStatus = $derived.by(() => {
    if (!product) return "";
    return getStockStatus(product.stockQuantity, product.lowStockThreshold);
  });
  let stockVariant = $derived.by(() => {
    if (!product) return "default" as const;
    return getStockVariant(product.stockQuantity, product.lowStockThreshold);
  });

  onMount(() => fetchProduct(params.id));

  async function fetchProduct(id: string | undefined) {
    loading = true;
    try {
      const res = await api.get<ProductResponse>(`/products/${id}`);
      product = res.data;
    } catch (err) {
      product = null;
    } finally {
      loading = false;
    }
    // Fetch reviews independently — failure here must not clear the product
    if (product) {
      fetchReviews(id);
    }
  }

  async function fetchReviews(productId: string | undefined) {
    if (!productId) return;
    reviewsLoading = true;
    try {
      const res = await getReviews(parseInt(productId));
      reviews = res.data;
    } catch (err) {
      reviews = [];
    } finally {
      reviewsLoading = false;
    }
  }

  async function handleAddReview() {
    if (!$auth) {
      toast.error("Please login to leave a review");
      return;
    }
    if (myReview) {
      toast.error("You have already reviewed this product");
      return;
    }
    if (!newReviewComment.trim()) {
      toast.error("Please enter a comment");
      return;
    }
    submittingReview = true;
    try {
      await createReview(product!.id, { rating: newReviewRating, comment: newReviewComment });
      newReviewComment = "";
      newReviewRating = 5;
      toast.success("Review added");
      await fetchReviews(params.id);
    } catch (err) {
      toast.error((err as any).response?.data?.message || "Failed to add review");
    } finally {
      submittingReview = false;
    }
  }

  function startEditReview() {
    if (!myReview) return;
    newReviewRating = myReview.rating;
    newReviewComment = myReview.comment;
    editingReview = true;
  }

  function cancelEditReview() {
    editingReview = false;
    newReviewRating = 5;
    newReviewComment = "";
  }

  async function handleUpdateReview() {
    if (!$auth || !myReview) return;
    if (!newReviewComment.trim()) {
      toast.error("Please enter a comment");
      return;
    }
    submittingReview = true;
    try {
      await updateReview(myReview.id, { rating: newReviewRating, comment: newReviewComment });
      editingReview = false;
      newReviewComment = "";
      newReviewRating = 5;
      toast.success("Review updated");
      await fetchReviews(params.id);
    } catch (err) {
      toast.error((err as any).response?.data?.message || "Failed to update review");
    } finally {
      submittingReview = false;
    }
  }

  async function handleDeleteReview(reviewId: number) {
    try {
      await deleteReview(reviewId);
      toast.success("Review deleted");
      await fetchReviews(params.id);
    } catch (err) {
      toast.error("Failed to delete review");
    }
  }

  function renderStars(rating: number) {
    const full = Math.floor(rating);
    const hasHalf = rating - full >= 0.5;
    const empty = 5 - full - (hasHalf ? 1 : 0);
    return { full, hasHalf, empty };
  }
</script>

<div class="px-4 py-6">
  {#if loading}
    <div class="flex flex-col gap-6 lg:flex-row">
      <Skeleton class="aspect-square w-full max-w-lg rounded-lg" />
      <div class="flex flex-1 flex-col gap-4">
        <Skeleton class="h-8 w-3/4" />
        <Skeleton class="h-4 w-1/3" />
        <Skeleton class="h-4 w-1/4" />
        <Skeleton class="h-10 w-1/3" />
        <Skeleton class="h-10 w-48" />
      </div>
    </div>
  {:else if !product}
    <div class="flex flex-col items-center gap-4 py-12">
      <p class="text-muted-foreground">Product not found.</p>
      <Button onclick={() => navigate("/products")}>
        <ArrowLeft class="mr-2 size-4" />
        Back to Catalog
      </Button>
    </div>
  {:else}
    <Button variant="ghost" class="mb-6" onclick={() => navigate("/products")}>
      <ArrowLeft class="mr-2 size-4" />
      Back to Catalog
    </Button>

    <div class="flex flex-col gap-8 lg:flex-row">
      <div class="w-full max-w-lg">
        <Card>
          <CardContent class="flex aspect-square items-center justify-center p-8">
            {#if product.imageUrl}
              <img src={product.imageUrl} alt={product.name} class="max-h-full max-w-full object-contain" />
            {:else}
              <div class="flex size-24 items-center justify-center text-muted-foreground">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" /></svg>
              </div>
            {/if}
          </CardContent>
        </Card>
      </div>

      <div class="flex-1">
        <div class="flex flex-col gap-3">
          <Badge variant="secondary">{product.categoryName}</Badge>
          <h1 class="text-2xl font-bold">{product.name}</h1>
          {#if product.manufacturer}
            <p class="text-muted-foreground">{product.manufacturer}</p>
          {/if}
          <p class="text-sm text-muted-foreground">SKU: {product.sku}</p>

          <div class="flex items-center gap-2">
            {#if product.rating != null && product.reviewCount > 0}
              {@const stars = renderStars(product.rating)}
              {#each { length: stars.full } as _, i}
                <Star class="size-4 fill-primary text-primary" />
              {/each}
              {#if stars.hasHalf}
                <Star class="size-4 fill-primary text-primary" />
              {/if}
              {#each { length: stars.empty } as _, i}
                <Star class="size-4 text-muted-foreground" />
              {/each}
              <span class="text-sm text-muted-foreground">{product.rating.toFixed(1)} ({product.reviewCount} reviews)</span>
            {:else}
              <span class="text-sm text-muted-foreground">No ratings yet</span>
            {/if}
          </div>

          <p class="text-3xl font-bold">{formatPrice(product.price)}</p>

          <Badge variant={stockVariant}>{stockStatus}</Badge>

          <div class="flex flex-wrap gap-3 pt-2">
            <Button onclick={() => cart.add(product as any)} disabled={product.stockQuantity === 0}>
              <ShoppingCart class="mr-2 size-4" />
              Add to Cart
            </Button>
            {#if product.datasheetUrl}
              <a href={product.datasheetUrl} target="_blank" rel="noopener noreferrer">
                <Button variant="outline">
                  <ExternalLink class="mr-2 size-4" />
                  View Datasheet
                </Button>
              </a>
            {/if}
          </div>
        </div>
      </div>
    </div>

    <Separator class="my-8" />

    <Tabs value="specs">
      <TabsList>
        <TabsTrigger value="specs">Specifications</TabsTrigger>
        <TabsTrigger value="description">Description</TabsTrigger>
        <TabsTrigger value="reviews">Reviews ({reviews.length})</TabsTrigger>
      </TabsList>

      <TabsContent value="specs" class="mt-4">
        {#if product.specs && Object.keys(product.specs).length > 0}
          <div class="rounded-lg border">
            {#each Object.entries(product.specs) as [key, value], i}
              <div class="flex border-b last:border-0 {i % 2 === 0 ? 'bg-muted/50' : ''}">
                <div class="w-1/3 border-r px-4 py-3 text-sm font-medium">{key}</div>
                <div class="flex-1 px-4 py-3 text-sm">{value}</div>
              </div>
            {/each}
          </div>
        {:else}
          <p class="text-muted-foreground">No specifications available.</p>
        {/if}
      </TabsContent>

      <TabsContent value="description" class="mt-4">
        {#if product.description}
          <p class="text-sm leading-relaxed">{product.description}</p>
        {:else}
          <p class="text-muted-foreground">No description available.</p>
        {/if}
      </TabsContent>

      <TabsContent value="reviews" class="mt-4">
        {#if reviewsLoading}
          <div class="flex flex-col gap-3">
            {#each { length: 3 } as _, i}
              <Skeleton class="h-16 w-full" />
            {/each}
          </div>
        {:else}
          {#if $auth}
            {#if editingReview && myReview}
              <div class="mb-6 flex flex-col gap-4 rounded-lg border p-4">
                <h3 class="font-medium">Edit Your Review</h3>
                <div class="flex items-center gap-2">
                  {#each { length: 5 } as _, i}
                    <button type="button" onclick={() => newReviewRating = i + 1}>
                      <Star class="size-5 {i < newReviewRating ? 'fill-primary text-primary' : 'text-muted-foreground'}" />
                    </button>
                  {/each}
                </div>
                <Textarea bind:value={newReviewComment} placeholder="Share your experience..." rows={3} />
                <div class="flex gap-2">
                  <Button onclick={handleUpdateReview} disabled={submittingReview}>
                    {#if submittingReview}
                      <Loader2 class="mr-2 size-4 animate-spin" />
                    {/if}
                    Update Review
                  </Button>
                  <Button variant="outline" onclick={cancelEditReview}>Cancel</Button>
                </div>
              </div>
            {:else if !myReview}
              <div class="mb-6 flex flex-col gap-4 rounded-lg border p-4">
                <h3 class="font-medium">Write a Review</h3>
                <div class="flex items-center gap-2">
                  {#each { length: 5 } as _, i}
                    <button type="button" onclick={() => newReviewRating = i + 1}>
                      <Star class="size-5 {i < newReviewRating ? 'fill-primary text-primary' : 'text-muted-foreground'}" />
                    </button>
                  {/each}
                </div>
                <Textarea bind:value={newReviewComment} placeholder="Share your experience..." rows={3} />
                <Button onclick={handleAddReview} disabled={submittingReview} class="self-start">
                  {#if submittingReview}
                    <Loader2 class="mr-2 size-4 animate-spin" />
                  {/if}
                  Submit Review
                </Button>
              </div>
            {:else}
              <div class="mb-6 flex items-center justify-between rounded-lg border bg-muted/30 p-4">
                <div>
                  <p class="text-sm font-medium">You have already reviewed this product.</p>
                  <p class="text-xs text-muted-foreground">Click edit to update your review.</p>
                </div>
                <Button variant="outline" size="sm" onclick={startEditReview}>Edit Review</Button>
              </div>
            {/if}
          {:else}
            <div class="mb-6 rounded-lg border p-4 text-center">
              <p class="text-sm text-muted-foreground">Please <Button variant="link" class="h-auto p-0" onclick={() => navigate('/login')}>login</Button> to leave a review.</p>
            </div>
          {/if}

          {#if reviews.length === 0}
            <p class="text-muted-foreground">No reviews yet. Be the first to review this product.</p>
          {:else}
            <div class="flex flex-col gap-4">
              {#each reviews as review}
                <Card>
                  <CardContent class="flex items-start gap-3 p-4">
                    <Avatar class="size-8">
                      <AvatarFallback class="text-xs">{(review.userEmail || "?").charAt(0).toUpperCase()}</AvatarFallback>
                    </Avatar>
                    <div class="flex-1">
                      <div class="flex items-center justify-between">
                        <div>
                          <span class="text-sm font-medium">{review.userEmail || "Anonymous"}</span>
                          <div class="flex items-center gap-1">
                            {#each { length: review.rating || 0 } as _, i}
                              <Star class="size-3 fill-primary text-primary" />
                            {/each}
                          </div>
                        </div>
                        <div class="flex items-center gap-2">
                          <span class="text-xs text-muted-foreground">
                            {#if review.createdAt}
                              {new Date(review.createdAt).toLocaleDateString()}
                            {:else}
                              -
                            {/if}
                          </span>
                          {#if $auth && ($auth.role === "ADMIN" || $auth.email === review.userEmail)}
                            <Button variant="ghost" size="icon" class="size-6" onclick={() => handleDeleteReview(review.id)}>
                              <Trash2 class="size-3.5 text-muted-foreground" />
                            </Button>
                          {/if}
                        </div>
                      </div>
                      <p class="mt-1 text-sm">{review.comment || ""}</p>
                    </div>
                  </CardContent>
                </Card>
              {/each}
            </div>
          {/if}
        {/if}
      </TabsContent>
    </Tabs>
  {/if}
</div>

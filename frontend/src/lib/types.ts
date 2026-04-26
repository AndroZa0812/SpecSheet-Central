export interface Category {
  id: number;
  name: string;
}

export interface ProductResponse {
  id: number;
  name: string;
  sku: string;
  price: number;
  stockQuantity: number;
  lowStockThreshold: number;
  categoryName: string;
  categoryId?: number;
  manufacturer: string | null;
  imageUrl: string | null;
  datasheetUrl: string | null;
  datasheetFilename: string | null;
  specs: Record<string, string> | null;
  description: string | null;
  rating: number;
  reviewCount: number;
}

export interface Review {
  id: number;
  productId: number;
  userEmail: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface AuthUser {
  email: string;
  role: "USER" | "ADMIN";
}

export interface AuthResponse {
  token: string;
  email: string;
  role: "USER" | "ADMIN";
}

interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  priceAtPurchase: number;
}

export interface OrderResponse {
  id: number;
  userEmail: string;
  orderDate: string;
  totalAmount: number;
  status: "PENDING" | "IN_DELIVERY" | "DELIVERED" | "CANCELLED";
  items: OrderItemResponse[];
}

export interface CartItem extends ProductResponse {
  quantity: number;
}

export interface RouteParams {
  id?: string;
  [key: string]: string | undefined;
}

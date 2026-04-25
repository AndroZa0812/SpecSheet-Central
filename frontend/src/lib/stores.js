import { writable } from 'svelte/store'

function createAuthStore() {
  const stored = localStorage.getItem('user')
  const { subscribe, set } = writable(stored ? JSON.parse(stored) : null)

  return {
    subscribe,
    login: (user, token) => {
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      set(user)
    },
    logout: () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      set(null)
    }
  }
}

export const auth = createAuthStore()

function createCartStore() {
  const stored = localStorage.getItem('cart')
  const { subscribe, set, update } = writable(stored ? JSON.parse(stored) : [])

  return {
    subscribe,
    add: (product) => update(items => {
      const existing = items.find(i => i.id === product.id)
      if (existing) {
        existing.quantity += 1
      } else {
        items.push({ ...product, quantity: 1 })
      }
      localStorage.setItem('cart', JSON.stringify(items))
      return [...items]
    }),
    remove: (productId) => update(items => {
      const filtered = items.filter(i => i.id !== productId)
      localStorage.setItem('cart', JSON.stringify(filtered))
      return filtered
    }),
    updateQuantity: (productId, quantity) => update(items => {
      const item = items.find(i => i.id === productId)
      if (item) {
        item.quantity = quantity
        if (quantity <= 0) {
          return items.filter(i => i.id !== productId)
        }
      }
      localStorage.setItem('cart', JSON.stringify(items))
      return [...items]
    }),
    clear: () => {
      localStorage.removeItem('cart')
      set([])
    }
  }
}

export const cart = createCartStore()

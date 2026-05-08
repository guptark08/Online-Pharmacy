# Pharmacy Frontend

This folder adds a full React frontend for the existing online pharmacy backend. It is built with Vite, React Router, Redux Toolkit, and the gateway APIs already present in the repository.

## Stack

- React with JSX and functional components
- React Router with nested routes, dynamic routes, URL params, and programmatic navigation
- Redux Toolkit with slices, selectors, async thunks, and global state
- Fetch API integration with loading and error states
- Responsive CSS with reusable layout and component patterns

## Folder Structure

```text
pharmacy-frontend/
  src/
    api/
    components/
    hooks/
    layouts/
    pages/
    router/
    store/
    styles/
    utils/
```

## Implemented Routes

- `/` home landing page
- `/catalog` medicine list with filters
- `/catalog/:medicineId` dynamic medicine detail
- `/login` and `/signup`
- `/cart`
- `/account/profile`
- `/account/prescriptions`
- `/account/orders`
- `/account/orders/:orderId`
- `/account/checkout`
- `/admin/dashboard`
- `/admin/orders`
- `/admin/users`
- `/admin/reports`

## Criteria Mapping

### React Setup & Fundamentals

- Vite app setup: [package.json](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/package.json), [vite.config.js](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/vite.config.js)
- JSX + functional components: all files in [src/components](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/components) and [src/pages](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/pages)
- Props and reusable components: `Button`, `Card`, `FormField`, `MedicineCard`, `QuantityControl`

### Component Basics

- Passing props: `MedicineCard`, `FilterBar`, `StatusBadge`, `ProtectedRoute`
- Default props and children: `Card`, `Button`, `EmptyState`, `SectionHeading`
- Event handling: catalog filters, cart quantity controls, login/signup forms, admin updates
- Controlled inputs: [LoginPage.jsx](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/pages/LoginPage.jsx), [SignupPage.jsx](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/pages/SignupPage.jsx), [CheckoutPage.jsx](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/pages/CheckoutPage.jsx)

### State & Rendering

- `useState`: forms, admin edit state, medicine quantity, checkout selections
- Handling form inputs: all auth, filter, checkout, admin pages
- Rendering lists: medicines, orders, prescriptions, users, reports
- Conditional rendering: loading, empty, error, role-based sections
- `useEffect`: fetching data on mount and reacting to state changes

### React Router Basics

- Installation and setup: [package.json](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/package.json)
- Pages: files in [src/pages](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/pages)
- Navigation with `Link` and `NavLink`: layouts, home cards, orders list
- Layout components: [AppLayout.jsx](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/layouts/AppLayout.jsx), [AccountLayout.jsx](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/layouts/AccountLayout.jsx), [AdminLayout.jsx](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/layouts/AdminLayout.jsx)

### React Router Advanced

- Dynamic routes: `/catalog/:medicineId`, `/account/orders/:orderId`
- Nested routes: `/account/*` and `/admin/*`
- URL params: `useParams` in medicine detail and order detail pages
- Programmatic navigation: login redirect, add-to-cart, checkout completion

### Component Architecture

- Clear folder structure using `components/`, `pages/`, `layouts/`, `hooks/`, `store/`
- Reusable component patterns: common UI building blocks and slice-driven page composition

### Redux Toolkit Basics

- Store setup: [src/store/index.js](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/store/index.js)
- Slices and reducers: [src/store/slices](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/store/slices)
- `useSelector` and `useDispatch`: wrapped via hooks in [src/hooks](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/hooks)

### Redux Toolkit Advanced

- Selectors with `createSelector`: auth, catalog, cart, orders, admin slices
- Async thunks: login, signup, medicines, cart actions, checkout, payment, reports
- State structure patterns: feature slices with request states and data groups
- Redux + Router: protected routes, login redirects, post-checkout navigation

### API Integration

- Fetch API wrapper: [client.js](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/api/client.js)
- GET and POST calls: catalog, auth, cart, address, order, admin flows
- Error handling and loading states: `ErrorAlert`, `LoadingState`, `EmptyState`
- Redux thunks for API calls: all slice files in `src/store/slices`

### Project Completion

- Page flows completed for customer and admin sides
- Global state plus local state used where appropriate
- Responsive layout included in [index.css](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-frontend/src/styles/index.css)
- Cleanup and organization included through shared utils/hooks/components

## Local Setup

1. Open a terminal in `pharmacy-frontend`.
2. Run `npm install`.
3. Run `npm run dev`.
4. Make sure the backend gateway is available at `http://localhost:8080`.

If you want to call another backend URL, create `.env` and set:

```env
VITE_API_BASE_URL=http://your-gateway-host:8080
```

## Demo Credentials

- Admin: `admin@pharmacy.local` / `vikash123`
- Customer: `customer1@pharmacy.local` / `vikash123`

## Deployment Notes

### Netlify

1. Build command: `npm run build`
2. Publish directory: `dist`
3. Add environment variable `VITE_API_BASE_URL` pointing to the deployed gateway URL

### Vercel

1. Framework preset: `Vite`
2. Build command: `npm run build`
3. Output directory: `dist`
4. Add `VITE_API_BASE_URL` in project environment variables

## Backend Connection

The gateway already exposes CORS for all origins in [pharmacy-gateway/src/main/resources/application.yml](/C:/Users/ASUS/Desktop/Online%20Pharmacy%20and%20Medicine%20Deliveryy/pharmacy-gateway/src/main/resources/application.yml), so the frontend can run separately during development and deployment.

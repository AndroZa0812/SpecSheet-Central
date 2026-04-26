# Datasheet Upload & Local Storage Design

## Date
2026-04-26

## Overview
Enable products to store their datasheets as PDFs locally on the server. Admins can either upload a PDF file directly or provide a URL (which the backend fetches and stores locally). Users always download/view datasheets from the local server, never from external URLs.

## Goals
- All datasheets are served from our own infrastructure.
- No reliance on external URLs that can break or change.
- Simple admin UX: upload file or paste URL.
- Automatic cleanup of old files when replaced or product deleted.

## Architecture

### Backend
- **Storage**: Local filesystem at `uploads/datasheets/`
- **New field**: `Product.datasheetFilename` (String, stored in DB)
- **New endpoint**: `GET /api/datasheets/{filename}` — serves PDF with `Content-Type: application/pdf` and `Content-Disposition: inline`
- **Product create/update**: Accept `multipart/form-data`
  - `datasheetFile`: multipart file upload
  - `datasheetUrl`: URL string to fetch
  - If `datasheetFile` present → save directly
  - Else if `datasheetUrl` present → fetch via HTTP client, save locally
  - Else if both absent and product had old file → delete old file
- **Cleanup**: On product update (new datasheet) or delete, delete old PDF file from disk.

### Frontend
- **ProductFormModal**: Add file input (`<input type="file" accept=".pdf">`). Add text input for "Fetch from URL". Show current filename if exists. Allow clearing datasheet.
  - Form submission uses `multipart/form-data` instead of JSON.
- **ProductDetail**: "View Datasheet" button links to `/api/datasheets/{filename}`. If no datasheet, hide button.

### Database
- Migration: add `datasheet_filename VARCHAR(255)` to `products` table.
- Existing `datasheet_url` column: deprecated but kept for reference; no longer used by frontend.

## Security Considerations
- Filename sanitization: store files with UUID or sanitized names to prevent path traversal.
- Content-Type validation: only accept `application/pdf`.
- URL fetching: validate HTTP response content-type before saving. Set reasonable timeouts.
- Access control: `GET /api/datasheets/{filename}` should be public (no auth needed) since product catalog is public.

## Error Handling
- Invalid PDF upload → 400 Bad Request
- URL fetch fails → 400 with message "Failed to fetch datasheet from URL"
- File not found on disk → 404

## Testing
- Upload PDF via multipart
- Provide URL and verify backend fetches and saves
- Verify old file deleted on replacement
- Verify file deleted on product delete
- Verify PDF served correctly with correct headers

## Non-Goals
- No cloud storage (S3, etc.) — local filesystem only for now.
- No PDF content extraction or indexing.
- No automatic periodic re-fetching of URL-based datasheets.

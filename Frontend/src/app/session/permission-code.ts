export const PermissionCode = {
  lendBook: 'loan.lend',
  returnBook: 'loan.return',
  searchCatalog: 'catalog.search',
  reserveBook: 'reservation.create',
} as const;

export type PermissionCode = (typeof PermissionCode)[keyof typeof PermissionCode];

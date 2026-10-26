export const PermissionCode = {
  lendBook: 'loan.lend',
  returnBook: 'loan.return',
  searchCatalog: 'catalog.search',
  reserveBook: 'reservation.create',
  viewAccount: 'account.view',
  payFine: 'fine.pay',
} as const;

export type PermissionCode = (typeof PermissionCode)[keyof typeof PermissionCode];

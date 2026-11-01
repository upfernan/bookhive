export const PermissionCode = {
  lendBook: 'loan.lend',
  returnBook: 'loan.return',
  searchCatalog: 'catalog.search',
  reserveBook: 'reservation.create',
  viewAccount: 'account.view',
  payFine: 'fine.pay',
  manageCatalog: 'catalog.manage',
  manageCategories: 'category.manage',
  locateBookCopy: 'book-copy.locate',
  relocateBookCopy: 'book-copy.relocate',
  withdrawBookCopy: 'book-copy.withdraw',
  configureInstitution: 'institution.configure',
  manageUsers: 'user.manage',
  syncCatalog: 'catalog.sync',
} as const;

export type PermissionCode = (typeof PermissionCode)[keyof typeof PermissionCode];

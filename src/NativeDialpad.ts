import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface ContactMap {
  rawId?: number;
  contactId?: number;
  name?: string;
  photoUri?: string;
  prefix?: string;
  firstName?: string;
  middleName?: string;
  surname?: string;
  suffix?: string;
  nickname?: string;
  thumbnailUri?: string;
  notes?: string;
  source?: string;
  starred?: number;
  mimetype?: string;
  ringtone?: string;
  phoneNumbers?: Array<{
    value: string;
    type: number;
    label: string;
    normalizedNumber: string;
    isPrimary: boolean;
  }>;
  emails?: Array<{
    value: string;
    type: number;
    label: string;
  }>;
  addresses?: Array<{
    value: string;
    type: number;
    label: string;
  }>;
  events?: Array<{
    value: string;
    type: number;
  }>;
  birthdays?: Array<string>;
  anniversaries?: Array<string>;
  groups?: Array<{
    id?: number;
    title: string;
  }>;
  organization?: {
    company: string;
    title: string;
  };
  websites?: Array<string>;
  IMs?: Array<{
    value: string;
    type: number;
    label: string;
  }>;
}

export interface Spec extends TurboModule {
  // Changed 'double' to 'number' to align with Codegen's supported types; maps to Double in Kotlin
  multiply(a: number, b: number): number;
  requestRole(): Promise<string>;
  makeCall(phoneNumber: string): Promise<string>;
  toggleSecureNumber(value: boolean): Promise<string>;
  getSecureNumber(): Promise<boolean>;
  toggleVibration(value: boolean): Promise<string>;
  getVibrationStatus(): Promise<boolean>;
  // subscriptionId is number (integer), maps to Int in Kotlin
  forwardAllCalls(
    cfi: boolean,
    phoneNumber: string,
    countryCode: string | null,
    subscriptionId: number
  ): Promise<string>;
  saveReplies(reply: string): Promise<string>;
  updateReplies(replies: Array<string>): Promise<string>;
  deleteReply(reply: string): Promise<string>;
  getReplies(): Promise<Array<string>>;
  getAllContacts(): Promise<
    Array<{
      rawId: number;
      contactId: number;
      name: string;
      photoUri: string;
      phoneNumbers: Array<{
        value: string;
        type: number;
        label: string;
        normalizedNumber: string;
        isPrimary: boolean;
      }>;
      birthdays: Array<string>;
      anniversaries: Array<string>;
    }>
  >;
  // rawContactId is number (integer), maps to Long in Kotlin
  getContactById(rawContactId: number): Promise<ContactMap>;
  createNewContact(contactMap: ContactMap): Promise<string>;
  // photoStatus is number (integer), maps to Int in Kotlin
  updateContact(contactMap: ContactMap, photoStatus: number): Promise<string>;
  deleteContact(contact: ContactMap): Promise<string>;
  isNumberBlocked(phoneNumber: string): Promise<boolean>;
  getBlockedNumbers(): Promise<Array<string>>;
  addBlockedNumber(number: string): Promise<string>;
  removeBlockedNumber(number: string): Promise<string>;
  toggleShowBlockNotification(): Promise<string>;
  getBlockNotificationStatus(): Promise<boolean>;
  getCallLogs(): Promise<
    Array<{
      number: string;
      type: number;
      date: number;
      duration: number;
      name: string;
    }>
  >;
  getDefaultDialerPackage(): Promise<string>;
  checkIfDefaultDialer(): Promise<boolean>;
  openDialerSetting(): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Dialpad');

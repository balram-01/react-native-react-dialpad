import Dialpad from './NativeDialpad';
export { default as ReactDialpadView } from './ReactDialpadViewNativeComponent';
export * from './ReactDialpadViewNativeComponent';
// Define ContactMap interface to match the structure expected by the native module
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

// Synchronous method
export function multiply(a: number, b: number): number {
  return Dialpad.multiply(a, b);
}

// Asynchronous methods
export async function requestRole(): Promise<string> {
  return await Dialpad.requestRole();
}

export async function makeCall(phoneNumber: string): Promise<string> {
  return await Dialpad.makeCall(phoneNumber);
}

export async function toggleSecureNumber(value: boolean): Promise<string> {
  return await Dialpad.toggleSecureNumber(value);
}

export async function getSecureNumber(): Promise<boolean> {
  return await Dialpad.getSecureNumber();
}

export async function toggleVibration(value: boolean): Promise<string> {
  return await Dialpad.toggleVibration(value);
}

export async function getVibrationStatus(): Promise<boolean> {
  return await Dialpad.getVibrationStatus();
}

export async function forwardAllCalls(
  cfi: boolean,
  phoneNumber: string,
  countryCode: string | null,
  subscriptionId: number
): Promise<string> {
  return await Dialpad.forwardAllCalls(
    cfi,
    phoneNumber,
    countryCode,
    subscriptionId
  );
}

export async function saveReplies(reply: string): Promise<string> {
  return await Dialpad.saveReplies(reply);
}

export async function updateReplies(replies: Array<string>): Promise<string> {
  return await Dialpad.updateReplies(replies);
}

export async function deleteReply(reply: string): Promise<string> {
  return await Dialpad.deleteReply(reply);
}

export async function getReplies(): Promise<Array<string>> {
  return await Dialpad.getReplies();
}

export async function getAllContacts(): Promise<
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
> {
  return await Dialpad.getAllContacts();
}

export async function getContactById(
  rawContactId: number
): Promise<ContactMap> {
  return await Dialpad.getContactById(rawContactId);
}

export async function createNewContact(
  contactMap: ContactMap
): Promise<string> {
  return await Dialpad.createNewContact(contactMap);
}

export async function updateContact(
  contactMap: ContactMap,
  photoStatus: number
): Promise<string> {
  return await Dialpad.updateContact(contactMap, photoStatus);
}

export async function deleteContact(contact: ContactMap): Promise<string> {
  return await Dialpad.deleteContact(contact);
}

export async function isNumberBlocked(phoneNumber: string): Promise<boolean> {
  return await Dialpad.isNumberBlocked(phoneNumber);
}

export async function getBlockedNumbers(): Promise<Array<string>> {
  return await Dialpad.getBlockedNumbers();
}

export async function addBlockedNumber(number: string): Promise<string> {
  return await Dialpad.addBlockedNumber(number);
}

export async function removeBlockedNumber(number: string): Promise<string> {
  return await Dialpad.removeBlockedNumber(number);
}

export async function toggleShowBlockNotification(): Promise<string> {
  return await Dialpad.toggleShowBlockNotification();
}

export async function getBlockNotificationStatus(): Promise<boolean> {
  return await Dialpad.getBlockNotificationStatus();
}

export async function getCallLogs(): Promise<
  Array<{
    number: string;
    type: number;
    date: number;
    duration: number;
    name: string;
  }>
> {
  return await Dialpad.getCallLogs();
}

export async function getDefaultDialerPackage(): Promise<string> {
  return await Dialpad.getDefaultDialerPackage();
}

export async function checkIfDefaultDialer(): Promise<boolean> {
  return await Dialpad.checkIfDefaultDialer();
}

export async function openDialerSetting(): Promise<string> {
  return await Dialpad.openDialerSetting();
}

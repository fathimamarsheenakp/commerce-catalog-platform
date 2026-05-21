/** Pass in react-router navigate state to show a toast after the next page loads. */
export function navigationToastState(variant, message) {
  return { toast: { variant, message } }
}

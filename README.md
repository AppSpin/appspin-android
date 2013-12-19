## AppSpin: Android ##
#### Helper classes for implementation ####
----------

This is a very simple implementation example for an **AppSpin** campaign. User action triggers `ASReferral` to check for campaigns for the specified app. 

If a campaign exists, the user is presented with the offer via a system `Dialog`. If they choose to participate, we launch the browser to display the campaign details.

The quickest implementations rely on our *white-labeled* webservice to display reward details, tracking links, etc. However, all data can be displayed natively to preserve branding and consistent UX

#### **Requirements** ####

 - The `ASReferral` helper class should be compatible from Android **API LEVEL 8** onward
 - You must include the Anddroid `Internet` permission in your app's `Manifest` file:
`<uses-permission android:name="android.permission.INTERNET"/>`

#### **Usage** ####

Within the relevant `Activity`:

`new ASReferral(myActivity.this).checkForOffers();`

If there aren't any offers or the campaigns have been stopped in the Admin Panel, the check will simply `return`.

#### **Notes** ####

 - Don't just display the offer immediately! Choose a behavioral `hook` in your application to initiate the campaign and then check for offers, e.g.

    - The second day the app is used
    - The first level or task has been completed
    - They click an '*Earn Rewards*' button in your menu
 - Use `res/values/strings.xml` to make offer messages easier to localize for internationally available apps 

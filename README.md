# Kingdoms
Conquer lands with a mighty Kingdom!

Welcoming new contributors. Report all issues in our issues tab.

### Compiling:

Kingdoms is an open source premium paid resource under Songoda, so for that reason, we as maintainers to this repository have added a permission requirement.
We use Github accounts to grant access in order to be able to compile, so if you're on our restricted list of users, you will not be allowed to compile this resource.

#### Getting a Github token:

Kingdoms' gradle is setup in a way where we make hiding your key local using enviromental variables.

1.) Go into your account settings on Github and create a personal token with the read:packages scope checked.

2.) Generate that key, and now go add a System Environment Variable named GITHUB_PACKAGES_KEY with the key as the value.

3.) Restart computer or if using Chocolatey type `refreshenv`

# Github Actions for mdbook-generation and fetching DBC-files from databricks.

Important: With the current fetch-script, all modules will be exported from databricks, but only modules given at the bottom `fetch-db.sh` will be processed and commited.

There is only one workflow connected to this repo. It is `main.yml`and it is responsible for fetching dbc-files and generating mdbooks that are automatically commited to your repo of choice. 

## You only need to do this the first time
1. Create a secret in settings(on repo page) ->  secrets -> actions -> New repository secret. 
Add a key called DB_KEY and paste the contents of your local .databrickscfg
2. Create additional secret called API_TOKEN_GITHUB and paste your github token.

3. (recommended) To be able to commit yml files from your local repo  to .github/workflow folder one needs to add workflow to its personal access token scope. Go to (account)settings -> developer settings -> personal access tokens -> your current token and edit. Add workflow under "select scope".
4. Change necessary fields in the `push to another repo`in `main.yml`. I recommend to change destination_repo, user_email, user_name and commit_message. Username and email could possibly be masked by using env variables. Should find something in docs. 

## You may need to edit and do this step many times...

5. In `fetch_dbc.sh` you can add and remove modules to be processed, acting for both fetch_dbc and mdbook-generate actions.

6. go to https://github.com/lamastex/scalable-data-science/actions and click "A workflow for mdbook generation" and "Run Workflow".




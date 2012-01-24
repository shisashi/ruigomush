from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

SAKURA = "http://shisashi.sakura.ne.jp/ruigomush/dictionary.db.gz"

class DictionaryPage(webapp.RequestHandler):
    def get(self):
        self.redirect(SAKURA)

    def head(self):
        self.redirect(SAKURA)

application = webapp.WSGIApplication(
                                     [('/dictionary.db.gz', DictionaryPage)],
                                     debug=False)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()


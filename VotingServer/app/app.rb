require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'

use Rack::PostBodyContentTypeParser

require 'app/GuiVotes'

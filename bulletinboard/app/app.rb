require 'sinatra'
require 'rack'
require 'rack/contrib'
require 'pp'

use Rack::PostBodyContentTypeParser

get '/' do
    $lastVote
end

post '/sendVote' do
    $lastVote = "received vote,  parameters were: id: #{params["id"]} data: #{params["data"]}\n"
end

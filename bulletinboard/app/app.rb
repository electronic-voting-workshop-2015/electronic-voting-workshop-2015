require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'
require 'pp'

use Rack::PostBodyContentTypeParser

get '/getVotingDomainParameters' do
   json( { group_type: "group-type-here", parameters: { param1: 1, param2: 2 } } )
end

get '/getVotingPublicKey' do
    json :voting_public_key => "voting_public_key here"
end

get '/getSignaturePublicKey/:kalpi_id' do
    kalpi_id = params[ "kalpi_id" ]
    json :kalpi_id => kalpi_id, public_key: "PUBLIC KEY FOR #{kalpi_id}"
end

post '/sendVote' do
    json :parameters_were => params
end
